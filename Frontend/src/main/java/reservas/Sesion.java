package reservas;

import reservas.logic.Funcionario;

public class Sesion {
    private static Funcionario usuario;   // el que devolvió el backend (sin clave)

    public static Funcionario getUsuario() {
        return usuario;
    }

    public static void setUsuario(Funcionario u) {
        usuario = u;
    }

    public static boolean isLoggedIn() {
        return usuario != null;
    }

    public static void logout() {
        usuario = null;
    }
}