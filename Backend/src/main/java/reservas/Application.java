package reservas;

import reservas.logic.Server;

public class Application {
    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}