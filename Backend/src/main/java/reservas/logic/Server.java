package reservas.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


 //Servidor del Backend.
 //Hilo principal (run): acepta conexiones SINCRONICAS (PORT); por cada una crea un Worker
 //(hilo propio) que atiende las peticiones de ese Frontend.
 //Hilo asincronico (runAsync): acepta la segunda conexion de cada Frontend (PORT_ASYNC), lee su
 //id de sesion y se la asigna al Worker correspondiente para que le envie notificaciones.

public class Server {
    ServerSocket srv;
    ServerSocket srvAsync;
    final List<Worker> workers = new ArrayList<Worker>();
    private final IService service;
    private volatile boolean continuar = true;

    public Server() throws IOException {
        this(Protocol.PORT, Protocol.PORT_ASYNC, Service.instance());
    }

    // Puerto 0 = el sistema escoge uno libre (util en pruebas)
    public Server(int port, int portAsync, IService service) throws IOException {
        this.service = service;
        srv = new ServerSocket(port);
        srvAsync = new ServerSocket(portAsync);
        System.out.println("Servidor iniciado (puertos " + getPort() + " y " + getPortAsync() + ")...");
    }

    public int getPort() { return srv.getLocalPort(); }
    public int getPortAsync() { return srvAsync.getLocalPort(); }

    public void run() {
        Thread tAsync = new Thread(new Runnable() {
            public void run() {
                runAsync();
            }
        }, "server-async");
        tAsync.setDaemon(true);
        tAsync.start();

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
                if (continuar) System.out.println(ex);
            }
        }
    }

    private void runAsync() {
        while (continuar) {
            try {
                Socket sa = srvAsync.accept();
                ObjectOutputStream osa = new ObjectOutputStream(sa.getOutputStream());
                osa.flush();
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
                if (continuar) System.out.println(ex);
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

    // Envia una notificacion a todos los Frontend con sesion iniciada, menos al que la origino.
    public void broadcast(int tipo, Object payload, Worker origen) {
        List<Worker> copia;
        synchronized (workers) {
            copia = new ArrayList<Worker>(workers);
        }
        for (Worker w : copia) {
            if (w != origen && w.isLogged()) w.notificar(tipo, payload);
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

    public int getWorkerCount() {
        synchronized (workers) {
            return workers.size();
        }
    }

    // Detiene el servidor: cierra los ServerSocket y todas las conexiones activas
    public void stop() {
        continuar = false;
        try { srv.close(); } catch (IOException ex) { }
        try { srvAsync.close(); } catch (IOException ex) { }
        List<Worker> copia;
        synchronized (workers) {
            copia = new ArrayList<Worker>(workers);
            workers.clear();
        }
        for (Worker w : copia) w.stop();
    }
}