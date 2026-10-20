package reservas.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class Worker {
    Server srv;
    Socket s;
    ObjectOutputStream os;
    ObjectInputStream is;
    IService service;

    // Canal asincrónico (alertas)
    final String sessionId = UUID.randomUUID().toString();
    Socket sAsync;
    ObjectOutputStream osAsync;
    private final Object asyncLock = new Object();

    public Worker(Server srv, Socket s, IService service) {
        try {
            this.srv = srv;
            this.s = s;
            this.service = service;
            os = new ObjectOutputStream(s.getOutputStream());
            is = new ObjectInputStream(s.getInputStream());
            // Lo primero que recibe el cliente es su sessionId
            os.writeObject(sessionId);
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

    // ===== Canal asincrónico =====
    public String getSessionId() {
        return sessionId;
    }

    public void setAsync(Socket sAsync, ObjectOutputStream osAsync) {
        synchronized (asyncLock) {
            this.sAsync = sAsync;
            this.osAsync = osAsync;
        }
    }

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

    public void listen() {
        int method;
        while (continuar) {
            try {
                method = is.readInt();
                System.out.println("Operacion: " + method);
                switch (method) {
                    // ===== A: LOGIN =====
                    case Protocol.LOGIN:
                        try {
                            Funcionario f = service.login((Funcionario) is.readObject());
                            ok(f);
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

                    // ===== B: FUNCIONARIOS / CATEGORÍAS / RESERVAS =====
                    // ===== C: RECURSOS / ACTIVIDADES / ESTADÍSTICAS =====

                    case Protocol.DISCONNECT:
                        stop();
                        srv.remove(this);
                        break;
                }
                os.flush();
            } catch (IOException e) {
                stop();
                srv.remove(this);
            }
        }
    }
}