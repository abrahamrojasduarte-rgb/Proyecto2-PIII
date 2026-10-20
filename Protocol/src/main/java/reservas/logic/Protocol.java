package reservas.logic;

public class Protocol {
    public static final String SERVER = "localhost";
    public static final int PORT = 1234;
    public static final int PORT_ASYNC = 1235;

    public static final int LOGIN = 101;
    public static final int CAMBIAR_CLAVE = 102;

    public static final int FUNCIONARIO_CREATE = 201;
    public static final int FUNCIONARIO_READ = 202;
    public static final int FUNCIONARIO_UPDATE = 203;
    public static final int FUNCIONARIO_DELETE = 204;
    public static final int FUNCIONARIO_FINDALL = 205;

    public static final int NOTIFY_RESERVA = 901;

    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_ERROR = 1;

    public static final int DISCONNECT = 99;
}