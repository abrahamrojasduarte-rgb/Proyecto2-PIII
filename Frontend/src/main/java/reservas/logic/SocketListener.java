package reservas.logic;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;


  // Hilo que "escucha" las notificaciones asincronicas del Backend por el segundo socket (ASYNC)
  // cada notificacion se entrega al AlertListener (un Controller) usando SwingUtilities.invokeLater,
  // para que la procese el EDT de Swing y no este hilo (Swing es mono-hilo)

public class SocketListener {
    private final AlertListener listener;
    private final String sid;     // id de sesion del canal sincronico
    private final Socket as;      // socket asincronico
    private final ObjectOutputStream aos;
    private final ObjectInputStream ais;

    private volatile boolean condition = true;
    private Thread t;

    public SocketListener(AlertListener listener, String sid, Configuracion cfg) throws Exception {
        this.listener = listener;
        this.sid = sid;
        as = new Socket();
        as.connect(new InetSocketAddress(cfg.getServer(), cfg.getPortAsync()), 5000);
        aos = new ObjectOutputStream(as.getOutputStream());
        aos.writeObject(sid);     // el Server lo usa para unir este socket con nuestro Worker
        aos.flush();
        ais = new ObjectInputStream(as.getInputStream());
    }

    public void start() {
        t = new Thread(new Runnable() {
            public void run() { listen(); }
        }, "socket-listener");
        t.setDaemon(true);
        condition = true;
        t.start();
    }

    public void stop() {
        condition = false;
        try { as.close(); } catch (IOException ignored) { }   // desbloquea el readInt
    }

    public boolean isActivo() {
        return t != null && t.isAlive();
    }

    private void listen() {
        int tipo;
        while (condition) {
            try {
                tipo = ais.readInt();
                Object payload = ais.readObject();
                deliver(tipo, payload);
            } catch (ClassNotFoundException ex) {
                System.out.println("Notificacion desconocida: " + ex.getMessage());
            } catch (IOException ex) {
                condition = false;      // el servidor cerro la conexion
            }
        }
        try {
            as.shutdownOutput();
            as.close();
        } catch (IOException ignored) { }
    }

    private void deliver(final int tipo, final Object payload) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { listener.alerta(tipo, payload); }
        });
    }
}