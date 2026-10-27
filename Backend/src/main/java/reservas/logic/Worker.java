package reservas.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public class Worker {
    Server srv;
    Socket s;
    ObjectOutputStream os;
    ObjectInputStream is;
    IService service;

    // Canal asincrónico: se une a este Worker mediante el sessionId
    final String sessionId = UUID.randomUUID().toString();
    Socket sAsync;
    ObjectOutputStream osAsync;
    private final Object asyncLock = new Object();

    volatile Funcionario usuario;   // quien inició sesión en esta conexión (null = nadie)

    public Worker(Server srv, Socket s, IService service) throws IOException {
        this.srv = srv;
        this.s = s;
        this.service = service;
        os = new ObjectOutputStream(s.getOutputStream());
        os.writeObject(sessionId);   // lo primero que recibe el cliente
        os.flush();
        s.setSoTimeout(5000);        // si el cliente no completa el saludo, no bloquea al servidor
        is = new ObjectInputStream(s.getInputStream());
        s.setSoTimeout(0);
    }

    private volatile boolean continuar;

    public void start() {
        System.out.println("Worker atendiendo peticiones...");
        Thread t = new Thread(new Runnable() {
            public void run() {
                listen();
            }
        }, "worker-" + sessionId.substring(0, 8));
        continuar = true;
        t.start();
    }

    public void stop() {
        continuar = false;
        try { s.close(); } catch (Exception ex) { }
        synchronized (asyncLock) {
            try { if (sAsync != null) sAsync.close(); } catch (Exception ex) { }
            sAsync = null;
            osAsync = null;
        }
        System.out.println("Conexion cerrada...");
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isLogged() {
        return usuario != null;
    }

    public void setAsync(Socket sAsync, ObjectOutputStream osAsync) {
        synchronized (asyncLock) {
            this.sAsync = sAsync;
            this.osAsync = osAsync;
        }
    }

    // Lo llama Server.broadcast() para enviar una alerta a este cliente
    public void notificar(int tipo, Object payload) {
        synchronized (asyncLock) {
            if (osAsync == null) return;
            try {
                osAsync.writeInt(tipo);
                osAsync.reset();
                osAsync.writeObject(payload);
                osAsync.flush();
            } catch (IOException ex) {
                System.out.println("No se pudo notificar: " + ex.getMessage());
            }
        }
    }

    // ===== Respuestas =====
    private void ok() throws IOException {
        os.writeInt(Protocol.ERROR_NO_ERROR);
    }

    private void ok(Object resultado) throws IOException {
        os.writeInt(Protocol.ERROR_NO_ERROR);
        os.reset();
        os.writeObject(resultado);
    }

    private void error(Exception ex) throws IOException {
        os.writeInt(Protocol.ERROR_ERROR);
        os.writeObject(ex.getMessage() == null ? "Error en el servidor" : ex.getMessage());
    }

    // ===== Permisos: se validan aquí, nunca se confía en el Frontend =====
    private void requireLogin() throws Exception {
        if (usuario == null) throw new Exception("Debe iniciar sesión");
    }

    private boolean esAdmin() {
        return usuario != null && Rol.ADMINISTRADOR.equals(usuario.getRol());
    }

    private void requireAdmin() throws Exception {
        requireLogin();
        if (!esAdmin()) throw new Exception("Solo un administrador puede ejecutar esta operación");
    }

    private void requireFuncionario() throws Exception {
        requireLogin();
        if (!Rol.FUNCIONARIO.equals(usuario.getRol()))
            throw new Exception("Solo un funcionario puede hacer reservas");
    }

    // OJO: en cada caso se LEEN los argumentos primero y DESPUES se valida el permiso.
    // Si se rechaza antes de leer, los bytes quedan en el stream y se desincroniza el protocolo.
    @SuppressWarnings("unchecked")
    public void listen() {
        int method;
        while (continuar) {
            try {
                method = is.readInt();
                System.out.println("Operacion: " + method);
                switch (method) {

                    // =============== LOGIN ===============
                    case Protocol.LOGIN:
                        try {
                            Funcionario f = (Funcionario) is.readObject();
                            usuario = null;
                            usuario = service.login(f);
                            ok(usuario);
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.CAMBIAR_CLAVE:
                        try {
                            Funcionario f = (Funcionario) is.readObject();
                            String claveNueva = (String) is.readObject();
                            service.cambiarClave(f, claveNueva);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;

                    // =============== FUNCIONARIOS (solo admin) ===============
                    case Protocol.FUNCIONARIO_CREATE:
                        try {
                            Funcionario f = (Funcionario) is.readObject();
                            requireAdmin();
                            service.create(f);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.FUNCIONARIO_UPDATE:
                        try {
                            Funcionario f = (Funcionario) is.readObject();
                            requireAdmin();
                            service.update(f);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.FUNCIONARIO_DELETE:
                        try {
                            String id = (String) is.readObject();
                            requireAdmin();
                            service.delete(id);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.FUNCIONARIO_FINDALL:
                        try {
                            requireAdmin();
                            ok(service.findAll());
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.FUNCIONARIO_SEARCH:
                        try {
                            String id = (String) is.readObject();
                            String nombre = (String) is.readObject();
                            requireAdmin();
                            ok(service.search(id, nombre));
                        } catch (Exception ex) { error(ex); }
                        break;

                    // =============== CATEGORIAS (escribe admin, lee cualquier logueado) ===============
                    case Protocol.CATEGORIA_CREATE:
                        try {
                            CategoriaRecurso c = (CategoriaRecurso) is.readObject();
                            requireAdmin();
                            ok(service.createCategoria(c));
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.CATEGORIA_UPDATE:
                        try {
                            CategoriaRecurso c = (CategoriaRecurso) is.readObject();
                            requireAdmin();
                            service.updateCategoria(c);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.CATEGORIA_DELETE:
                        try {
                            String id = (String) is.readObject();
                            requireAdmin();
                            service.deleteCategoria(id);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.CATEGORIA_FINDALL:
                        try {
                            requireLogin();
                            ok(service.findAllCategorias());
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.CATEGORIA_SEARCH:
                        try {
                            String desc = (String) is.readObject();
                            requireLogin();
                            ok(service.searchCategorias(desc));
                        } catch (Exception ex) { error(ex); }
                        break;

                    // =============== RECURSOS (escribe admin, lee cualquier logueado) ===============
                    case Protocol.RECURSO_CREATE:
                        try {
                            Recurso r = (Recurso) is.readObject();
                            requireAdmin();
                            service.createRecurso(r);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RECURSO_UPDATE:
                        try {
                            Recurso r = (Recurso) is.readObject();
                            requireAdmin();
                            service.updateRecurso(r);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RECURSO_DELETE:
                        try {
                            String id = (String) is.readObject();
                            requireAdmin();
                            service.deleteRecurso(id);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RECURSO_FINDALL:
                        try {
                            requireLogin();
                            ok(service.findAllRecursos());
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RECURSO_SEARCH:
                        try {
                            String categoriaId = (String) is.readObject();
                            String desc = (String) is.readObject();
                            requireLogin();
                            ok(service.searchRecursos(categoriaId, desc));
                        } catch (Exception ex) { error(ex); }
                        break;

                    // =============== RESERVAS ===============
                    case Protocol.RESERVA_CREATE:
                        try {
                            Reserva r = (Reserva) is.readObject();
                            List<CategoriaRecurso> categorias = (List<CategoriaRecurso>) is.readObject();
                            requireFuncionario();
                            r.setFuncionario(usuario);   // el dueno sale de la sesion, no del cliente
                            Reserva creada = service.createReserva(r, categorias);
                            ok(creada);
                            os.flush();
                            // Efecto colateral: notificacion asincronica a los DEMAS Frontend
                            srv.broadcast(Protocol.NOTIFY_RESERVA, AlertaReserva.de(creada), this);
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RESERVA_DELETE:
                        try {
                            int id = is.readInt();
                            requireLogin();
                            Reserva r = service.findReservaById(id);
                            if (!esAdmin() && !r.getFuncionario().getId().equals(usuario.getId()))
                                throw new Exception("Solo puede cancelar sus propias reservas");
                            service.deleteReserva(id);
                            ok();
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RESERVA_READ:
                        try {
                            int id = is.readInt();
                            requireLogin();
                            ok(service.findReservaById(id));
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RESERVA_FINDALL:
                        try {
                            requireLogin();
                            ok(service.findAllReservas());
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RESERVA_FUNCIONARIO:
                        try {
                            String id = (String) is.readObject();
                            requireLogin();
                            if (!esAdmin()) id = usuario.getId();   // un funcionario solo ve las suyas
                            ok(service.findReservasByFuncionario(id));
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RESERVA_SEARCH:
                        try {
                            LocalDate desde = (LocalDate) is.readObject();
                            LocalDate hasta = (LocalDate) is.readObject();
                            requireLogin();
                            ok(service.searchReservas(desde, hasta));
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RESERVA_EXTRAER:
                        try {
                            String frase = (String) is.readObject();
                            requireFuncionario();
                            ok(service.extraerReserva(frase));
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.DISCONNECT:
                        stop();
                        srv.remove(this);
                        return;

                    default:   // codigo desconocido: no sabemos cuantos bytes consumir, se cierra
                        stop();
                        srv.remove(this);
                        return;
                }
                os.flush();
            } catch (IOException e) {
                stop();
                srv.remove(this);
            }
        }
    }
}