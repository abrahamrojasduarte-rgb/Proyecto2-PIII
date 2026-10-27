package reservas.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Service implements IService {
    private static Service theInstance;

    // Crea (la primera vez) la conexion con el Backend segun frontend.properties
    public static synchronized Service instance() {
        if (theInstance == null) {
            try {
                theInstance = new Service(Configuracion.cargar());
            } catch (Exception ex) {
                throw new IllegalStateException(ex.getMessage(), ex);
            }
        }
        return theInstance;
    }

    private final Configuracion configuracion;
    private final Socket s;
    private final ObjectOutputStream os;
    private final ObjectInputStream is;
    private final String sid;   // id de sesion que asigna el Backend

    public Service(Configuracion configuracion) throws Exception {
        this.configuracion = configuracion;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(configuracion.getServer(), configuracion.getPort()), 5000);
            os = new ObjectOutputStream(s.getOutputStream());
            os.flush();
            is = new ObjectInputStream(s.getInputStream());
            sid = (String) is.readObject();          // lo primero que envia el Worker
        } catch (Exception ex) {
            throw new Exception("No se pudo conectar con el servidor " + configuracion.getServer()
                    + ":" + configuracion.getPort() + " (" + ex.getMessage() + ")");
        }
    }

    public String getSid() { return sid; }
    public Configuracion getConfiguracion() { return configuracion; }

    // ===================== Mecanica de envio =====================
    private void enviar(int operacion, Object... argumentos) throws IOException {
        os.writeInt(operacion);
        for (Object a : argumentos) {
            if (a instanceof Integer) {
                os.writeInt((Integer) a);
            } else {
                os.reset();          // si no, un objeto modificado se reenviaria con sus datos viejos
                os.writeObject(a);
            }
        }
        os.flush();
    }

    private void verificarEstado() throws Exception {
        int estado = is.readInt();
        if (estado != Protocol.ERROR_NO_ERROR) {
            throw new Exception((String) is.readObject());
        }
    }

    // Peticion sin resultado (solo estado)
    private synchronized void ejecutar(int operacion, Object... argumentos) throws Exception {
        try {
            enviar(operacion, argumentos);
            verificarEstado();
        } catch (IOException ex) {
            throw new Exception("Se perdió la conexión con el servidor");
        }
    }

    // Peticion con resultado
    @SuppressWarnings("unchecked")
    private synchronized <T> T consultar(int operacion, Object... argumentos) throws Exception {
        try {
            enviar(operacion, argumentos);
            verificarEstado();
            return (T) is.readObject();
        } catch (IOException ex) {
            throw new Exception("Se perdió la conexión con el servidor");
        }
    }

    // =============== LOGIN ===============
    @Override
    public Funcionario login(Funcionario f) throws Exception {
        return consultar(Protocol.LOGIN, f);
    }

    @Override
    public void cambiarClave(Funcionario f, String claveNueva) throws Exception {
        ejecutar(Protocol.CAMBIAR_CLAVE, f, claveNueva);
    }

    // =============== FUNCIONARIOS ===============
    @Override
    public void create(Funcionario f) throws Exception { ejecutar(Protocol.FUNCIONARIO_CREATE, f); }

    @Override
    public void update(Funcionario f) throws Exception { ejecutar(Protocol.FUNCIONARIO_UPDATE, f); }

    @Override
    public void delete(String id) throws Exception { ejecutar(Protocol.FUNCIONARIO_DELETE, id); }

    @Override
    public List<Funcionario> findAll() throws Exception { return consultar(Protocol.FUNCIONARIO_FINDALL); }

    @Override
    public List<Funcionario> search(String id, String nombre) throws Exception {
        return consultar(Protocol.FUNCIONARIO_SEARCH, texto(id), texto(nombre));
    }

    // =============== CATEGORIAS ===============
    @Override
    public CategoriaRecurso createCategoria(CategoriaRecurso c) throws Exception {
        return consultar(Protocol.CATEGORIA_CREATE, c);
    }

    @Override
    public void updateCategoria(CategoriaRecurso c) throws Exception { ejecutar(Protocol.CATEGORIA_UPDATE, c); }

    @Override
    public void deleteCategoria(String id) throws Exception { ejecutar(Protocol.CATEGORIA_DELETE, id); }

    @Override
    public List<CategoriaRecurso> findAllCategorias() throws Exception {
        return consultar(Protocol.CATEGORIA_FINDALL);
    }

    @Override
    public List<CategoriaRecurso> searchCategorias(String descripcion) throws Exception {
        return consultar(Protocol.CATEGORIA_SEARCH, texto(descripcion));
    }

    // =============== RECURSOS ===============
    @Override
    public void createRecurso(Recurso r) throws Exception { ejecutar(Protocol.RECURSO_CREATE, r); }

    @Override
    public void updateRecurso(Recurso r) throws Exception { ejecutar(Protocol.RECURSO_UPDATE, r); }

    @Override
    public void deleteRecurso(String id) throws Exception { ejecutar(Protocol.RECURSO_DELETE, id); }

    @Override
    public List<Recurso> findAllRecursos() throws Exception { return consultar(Protocol.RECURSO_FINDALL); }

    @Override
    public List<Recurso> searchRecursos(String categoriaId, String descripcion) throws Exception {
        return consultar(Protocol.RECURSO_SEARCH, texto(categoriaId), texto(descripcion));
    }

    // =============== RESERVAS ===============
    @Override
    public Reserva createReserva(Reserva r, List<CategoriaRecurso> categorias) throws Exception {
        return consultar(Protocol.RESERVA_CREATE, r, new ArrayList<>(categorias));
    }

    @Override
    public void deleteReserva(int id) throws Exception { ejecutar(Protocol.RESERVA_DELETE, id); }

    @Override
    public Reserva findReservaById(int id) throws Exception { return consultar(Protocol.RESERVA_READ, id); }

    @Override
    public List<Reserva> findAllReservas() throws Exception { return consultar(Protocol.RESERVA_FINDALL); }

    @Override
    public List<Reserva> findReservasByFuncionario(String idFuncionario) throws Exception {
        return consultar(Protocol.RESERVA_FUNCIONARIO, texto(idFuncionario));
    }

    @Override
    public List<Reserva> searchReservas(LocalDate desde, LocalDate hasta) throws Exception {
        if (desde == null || hasta == null) throw new Exception("Debe indicar las fechas desde y hasta");
        return consultar(Protocol.RESERVA_SEARCH, desde, hasta);
    }

    @Override
    public ReservaExtraccion extraerReserva(String frase) throws Exception {
        return consultar(Protocol.RESERVA_EXTRAER, texto(frase));
    }

    // =============== CIERRE ===============
    @Override
    public synchronized void stop() {
        try {
            os.writeInt(Protocol.DISCONNECT);
            os.flush();
        } catch (IOException ignored) { }
        try { s.close(); } catch (IOException ignored) { }
    }

    private static String texto(String s) { return s == null ? "" : s; }
}
