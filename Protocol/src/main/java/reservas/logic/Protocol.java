package reservas.logic;

public class Protocol {
    public static final String SERVER = "localhost";
    public static final int PORT = 1234;
    public static final int PORT_ASYNC = 1235;

    public static final int LOGIN = 101;
    public static final int CAMBIAR_CLAVE = 102;

    public static final int FUNCIONARIO_CREATE = 201;
    public static final int FUNCIONARIO_READ = 202;      // sin usar por ahora
    public static final int FUNCIONARIO_UPDATE = 203;
    public static final int FUNCIONARIO_DELETE = 204;
    public static final int FUNCIONARIO_FINDALL = 205;
    public static final int FUNCIONARIO_SEARCH = 206;

    public static final int CATEGORIA_CREATE = 301;
    public static final int CATEGORIA_UPDATE = 302;
    public static final int CATEGORIA_DELETE = 303;
    public static final int CATEGORIA_FINDALL = 304;
    public static final int CATEGORIA_SEARCH = 305;

    public static final int RECURSO_CREATE = 401;
    public static final int RECURSO_UPDATE = 402;
    public static final int RECURSO_DELETE = 403;
    public static final int RECURSO_FINDALL = 404;
    public static final int RECURSO_SEARCH = 405;

    public static final int RESERVA_CREATE = 501;
    public static final int RESERVA_FINDALL = 502;

    public static final int NOTIFY_RESERVA = 901;

    public static final int ERROR_NO_ERROR = 0;
    public static final int ERROR_ERROR = 1;

    public static final int DISCONNECT = 99;
}