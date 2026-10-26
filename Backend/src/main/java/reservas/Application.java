package reservas;

import reservas.data.Database;
import reservas.logic.Server;

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

        Server server = new Server();
        server.run();
    }
}