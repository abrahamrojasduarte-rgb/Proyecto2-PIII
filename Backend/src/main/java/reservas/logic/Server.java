package reservas.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    ServerSocket srv;
    ServerSocket srvAsync;
    List<Worker> workers;

    public Server() {
        try {
            srv = new ServerSocket(Protocol.PORT);
            srvAsync = new ServerSocket(Protocol.PORT_ASYNC);
            workers = new ArrayList<Worker>();
            System.out.println("Servidor iniciado (puertos " + Protocol.PORT + " y " + Protocol.PORT_ASYNC + ")...");
        } catch (IOException ex) {
            System.out.println(ex);
            System.exit(-1);
        }
    }

    public void run() {
        IService service = Service.instance();

        Thread tAsync = new Thread(new Runnable() {
            public void run() {
                runAsync();
            }
        });
        tAsync.setDaemon(true);
        tAsync.start();

        boolean continuar = true;
        Socket s;
        Worker worker;
        while (continuar) {
            try {
                s = srv.accept();
                System.out.println("Conexion Establecida...");
                worker = new Worker(this, s, service);
                this.add(worker);
                worker.start();
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private void runAsync() {
        while (true) {
            try {
                Socket sa = srvAsync.accept();
                ObjectOutputStream osa = new ObjectOutputStream(sa.getOutputStream());
                ObjectInputStream isa = new ObjectInputStream(sa.getInputStream());
                String sid = (String) isa.readObject();
                Worker w = find(sid);
                if (w != null) {
                    w.setAsync(sa, osa);
                    System.out.println("Canal asincrónico asociado.");
                } else {
                    sa.close();
                }
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private Worker find(String sid) {
        synchronized (workers) {
            for (Worker w : workers) {
                if (w.getSessionId().equals(sid)) return w;
            }
        }
        return null;
    }

    public void broadcast(int tipo, Object payload, Worker origen) {
        List<Worker> copia;
        synchronized (workers) {
            copia = new ArrayList<Worker>(workers);
        }
        for (Worker w : copia) {
            if (w != origen) w.notificar(tipo, payload);
        }
    }

    public void add(Worker w) {
        synchronized (workers) {
            workers.add(w);
            System.out.println("Quedan: " + workers.size());
        }
    }

    public void remove(Worker w) {
        synchronized (workers) {
            workers.remove(w);
            System.out.println("Quedan: " + workers.size());
        }
    }
}