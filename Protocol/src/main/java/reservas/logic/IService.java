package reservas.logic;

public interface IService {
    public Funcionario login(Funcionario f) throws Exception;
    public void cambiarClave(Funcionario f, String claveNueva) throws Exception;

    public void stop();
}