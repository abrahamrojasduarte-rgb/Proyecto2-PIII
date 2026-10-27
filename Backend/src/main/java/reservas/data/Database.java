package reservas.data;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;


public class Database {
    private static Database theInstance;

    public static synchronized Database instance() {
        if (theInstance == null) {
            theInstance = new Database();
        }
        return theInstance;
    }

    public static final String PROPERTIES_FILE_NAME = "/database.properties";
    Connection cnx;

    public Database() { }

    private long ultimaVerificacion;
    public synchronized Connection connection() throws SQLException {
        long ahora = System.currentTimeMillis();
        boolean verificar = ahora - ultimaVerificacion > 30_000;
        if (cnx == null || cnx.isClosed() || (verificar && !cnx.isValid(2))) {
            cnx = abrir();
        }
        if (verificar) ultimaVerificacion = ahora;
        return cnx;
    }

    private Connection abrir() throws SQLException {
        Properties prop = new Properties();
        try (InputStream in = getClass().getResourceAsStream(PROPERTIES_FILE_NAME)) {
            if (in == null) {
                throw new SQLException("No se encontro " + PROPERTIES_FILE_NAME
                        + " (copie database.properties.example como database.properties)");
            }
            prop.load(in);
        } catch (java.io.IOException ex) {
            throw new SQLException("No se pudo leer " + PROPERTIES_FILE_NAME + ": " + ex.getMessage());
        }
        String driver = prop.getProperty("database_driver");
        String server = prop.getProperty("database_server");
        String port = prop.getProperty("database_port");
        String user = prop.getProperty("database_user");
        String password = prop.getProperty("database_password");
        String database = prop.getProperty("database_name");

        String url = "jdbc:mysql://" + server + ":" + port + "/" + database
                + "?serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false";
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException ex) {
            throw new SQLException("No se encontro el driver " + driver);
        }
        return DriverManager.getConnection(url, user, password);
    }

    public PreparedStatement prepareStatement(String statement) throws SQLException {
        return connection().prepareStatement(statement);
    }

    public synchronized void close() throws SQLException {
        if (cnx != null && !cnx.isClosed()) {
            cnx.close();
        }
        cnx = null;
    }
}