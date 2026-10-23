package reservas.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    Funcionario usuario;   // quien inició sesión en esta conexión (null = nadie)

    public Worker(Server srv, Socket s, IService service) {
        try {
            this.srv = srv;
            this.s = s;
            this.service = service;
            os = new ObjectOutputStream(s.getOutputStream());
            is = new ObjectInputStream(s.getInputStream());
            os.writeObject(sessionId);   // lo primero que recibe el cliente
            os.flush();
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    boolean continuar;

    public void start() {
        try {
            System.out.println("Worker atendiendo peticiones...");
            Thread t = new Thread(new Runnable() {
                public void run() {
                    listen();
                }
            });
            continuar = true;
            t.start();
        } catch (Exception ex) { }
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

    private void requireAdmin() throws Exception {
        requireLogin();
        if (!"ADMINISTRADOR".equals(String.valueOf(usuario.getRol())))
            throw new Exception("Solo un administrador puede ejecutar esta operación");
    }

    private AlertaReserva alertaDe(Reserva r) {
        List<String> recursos = new ArrayList<>();
        for (Recurso rec : r.getRecursos()) recursos.add(rec.getDescripcion());
        return new AlertaReserva(r.getFuncionario().getNombre(), r.getDescripcion(),
                r.getFecha(), r.getDesde(), r.getHasta(), recursos);
    }

    // OJO: en cada caso se LEEN los argumentos primero y DESPUÉS se valida el permiso.
    // Si se rechaza antes de leer, los bytes quedan en el stream y se desincroniza el protocolo.
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
                            requireLogin();
                            r.setFuncionario(usuario);   // el dueño sale de la sesión, no del cliente
                            Reserva creada = service.createReserva(r);
                            ok(creada);
                            srv.broadcast(Protocol.NOTIFY_RESERVA, alertaDe(creada), this);
                        } catch (Exception ex) { error(ex); }
                        break;
                    case Protocol.RESERVA_FINDALL:
                        try {
                            requireLogin();
                            ok(service.findAllReservas());
                        } catch (Exception ex) { error(ex); }
                        break;

                    case Protocol.DISCONNECT:
                        stop();
                        srv.remove(this);
                        return;

                    default:   // código desconocido: no sabemos cuántos bytes consumir, se cierra
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