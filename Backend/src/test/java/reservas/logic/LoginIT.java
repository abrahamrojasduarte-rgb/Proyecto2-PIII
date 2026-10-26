package reservas.logic;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reservas.data.Database;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class LoginIT {
    static Socket s;
    static ObjectOutputStream os;
    static ObjectInputStream is;

    static boolean hayBaseDeDatos() {
        try {
            Database.instance().connection();
            return true;
        } catch (Exception ex) {
            System.out.println("LoginIT omitida: " + ex.getMessage());
            return false;
        }
    }

    @BeforeAll
    static void arrancar() throws Exception {
        assumeTrue(hayBaseDeDatos(), "No hay conexion a MySQL");
        Thread t = new Thread(() -> new Server().run());
        t.setDaemon(true);
        t.start();
        Thread.sleep(1500);   // tiempo para que abra los puertos

        s = new Socket("localhost", Protocol.PORT);
        os = new ObjectOutputStream(s.getOutputStream());   // primero el output
        os.flush();
        is = new ObjectInputStream(s.getInputStream());
        is.readObject();                                    // sessionId, se ignora
    }

    @AfterAll
    static void cerrar() throws Exception {
        if (s == null) return;
        os.writeInt(Protocol.DISCONNECT);
        os.flush();
        s.close();
    }

    private void enviarLogin(String id, String clave) throws Exception {
        Funcionario f = new Funcionario();
        f.setId(id);
        f.setClave(clave);
        os.writeInt(Protocol.LOGIN);
        os.reset();
        os.writeObject(f);
        os.flush();
    }

    @Test
    void loginCorrecto() throws Exception {
        enviarLogin("222", "222");
        assertEquals(Protocol.ERROR_NO_ERROR, is.readInt());
        Funcionario f = (Funcionario) is.readObject();
        assertEquals("222", f.getId());
        assertEquals("Maria Mata", f.getNombre());
        assertEquals("", f.getClave());   // la clave no regresa al cliente
    }

    @Test
    void loginAdministrador() throws Exception {
        enviarLogin("admin", "admin123");
        assertEquals(Protocol.ERROR_NO_ERROR, is.readInt());
        assertEquals(Rol.ADMINISTRADOR, ((Funcionario) is.readObject()).getRol());
    }

    @Test
    void claveIncorrecta() throws Exception {
        enviarLogin("222", "xxx");
        assertEquals(Protocol.ERROR_ERROR, is.readInt());
        assertEquals("Clave incorrecta", is.readObject());
    }

    @Test
    void usuarioInexistente() throws Exception {
        enviarLogin("999", "1");
        assertEquals(Protocol.ERROR_ERROR, is.readInt());
        assertEquals("Funcionario no existe", is.readObject());
    }

    @Test
    void usuarioVacio() throws Exception {
        enviarLogin("", "1");
        assertEquals(Protocol.ERROR_ERROR, is.readInt());
        assertEquals("Debe indicar el usuario", is.readObject());
    }
}