package reservas.logic;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class LoginIT {
    static Socket s;
    static ObjectOutputStream os;
    static ObjectInputStream is;

    @BeforeAll
    static void arrancar() throws Exception {
        Thread t = new Thread(() -> new Server().run());
        t.setDaemon(true);
        t.start();
        Thread.sleep(1500);   // tiempo para que abra los puertos

        s = new Socket("localhost", Protocol.PORT);
        os = new ObjectOutputStream(s.getOutputStream());   // primero el output
        is = new ObjectInputStream(s.getInputStream());
        is.readObject();                                    // sessionId, se ignora
    }

    @AfterAll
    static void cerrar() throws Exception {
        os.writeInt(Protocol.DISCONNECT);
        os.flush();
        s.close();
    }

    private void enviarLogin(String id, String clave) throws Exception {
        os.writeInt(Protocol.LOGIN);
        os.writeObject(new Funcionario(id, "", clave, ""));
        os.flush();
    }

    @Test
    void loginCorrecto() throws Exception {
        enviarLogin("222", "222");
        assertEquals(Protocol.ERROR_NO_ERROR, is.readInt());
        Funcionario f = (Funcionario) is.readObject();
        assertEquals("222", f.getId());
        assertEquals("", f.getClave());   // la clave no regresa al cliente
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