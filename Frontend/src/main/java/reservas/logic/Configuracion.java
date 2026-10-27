package reservas.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


public class Configuracion {
    private final String server;
    private final int port;
    private final int portAsync;

    public Configuracion(String server, int port, int portAsync) {
        this.server = server;
        this.port = port;
        this.portAsync = portAsync;
    }

    public String getServer() { return server; }
    public int getPort() { return port; }
    public int getPortAsync() { return portAsync; }

    public static Configuracion cargar() {
        Properties p = new Properties();
        try (InputStream in = Configuracion.class.getResourceAsStream("/frontend.properties")) {
            if (in != null) p.load(in);
        } catch (Exception ignored) { }

        File externo = new File("frontend.properties");
        if (externo.isFile()) {
            try (InputStream in = new FileInputStream(externo)) {
                p.load(in);
            } catch (Exception ignored) { }
        }
        return desde(p, System.getProperties());
    }

    /** Combina las propiedades del archivo con las del sistema (separado para poder probarlo). */
    static Configuracion desde(Properties archivo, Properties sistema) {
        String server = valor("server", archivo, sistema, Protocol.SERVER);
        int port = entero(valor("port", archivo, sistema, null), Protocol.PORT);
        int portAsync = entero(valor("port_async", archivo, sistema, null), Protocol.PORT_ASYNC);
        return new Configuracion(server, port, portAsync);
    }

    private static String valor(String clave, Properties archivo, Properties sistema, String defecto) {
        String v = sistema.getProperty(clave);
        if (v == null || v.isBlank()) v = archivo.getProperty(clave);
        return (v == null || v.isBlank()) ? defecto : v.trim();
    }

    private static int entero(String v, int defecto) {
        try {
            return v == null ? defecto : Integer.parseInt(v.trim());
        } catch (NumberFormatException ex) {
            return defecto;
        }
    }

    @Override
    public String toString() { return server + ":" + port + " (async " + portAsync + ")"; }
}