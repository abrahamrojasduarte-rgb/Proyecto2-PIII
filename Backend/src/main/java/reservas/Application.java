package reservas;

import reservas.data.Database;
import reservas.logic.Server;
import reservas.logic.Service;

public class Application {
    public static void main(String[] args) {
        // Verificar la base de datos antes de aceptar clientes
        try {
            Database.instance().connection();
            System.out.println("Conectado a la base de datos.");
        } catch (Exception ex) {
            System.err.println("No se pudo conectar a la base de datos: " + ex.getMessage());
            System.exit(-1);
        }

        try {
            Server server = new Server();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.stop();
                Service.instance().stop();
            }));
            server.run();
        } catch (Exception ex) {
            System.err.println("No se pudo iniciar el servidor: " + ex.getMessage());
            System.exit(-1);
        }
    }
}