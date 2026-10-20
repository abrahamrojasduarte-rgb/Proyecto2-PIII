package reservas.logic;

import reservas.data.Database;
import reservas.data.FuncionarioDao;

public class Service implements IService {
    private static Service theInstance;

    public static synchronized IService instance() {
        if (theInstance == null) theInstance = new Service();
        return theInstance;
    }

    private FuncionarioDao funcionarioDao;

    private Service() {
        try {
            funcionarioDao = new FuncionarioDao();
        } catch (Exception e) {
            System.exit(-1);
        }
    }

    // =============== LOGIN ===============
    @Override
    public Funcionario login(Funcionario f) throws Exception {
        if (f == null || f.getId() == null || f.getId().isBlank())
            throw new Exception("Debe indicar el usuario");
        if (f.getClave() == null || f.getClave().isBlank())
            throw new Exception("Debe indicar la clave");

        Funcionario real = funcionarioDao.read(f.getId());
        if (!real.getClave().equals(f.getClave()))
            throw new Exception("Clave incorrecta");

        real.setClave("");   // la clave no viaja de regreso al Frontend
        return real;
    }

    @Override
    public void cambiarClave(Funcionario f, String claveNueva) throws Exception {
        if (claveNueva == null || claveNueva.isBlank())
            throw new Exception("La clave nueva no puede estar vacía");
        if (claveNueva.length() < 3)
            throw new Exception("La clave nueva debe tener al menos 3 caracteres");

        Funcionario real = funcionarioDao.read(f.getId());
        if (!real.getClave().equals(f.getClave()))
            throw new Exception("La clave actual es incorrecta");

        funcionarioDao.updateClave(f.getId(), claveNueva);
    }

    // =============== B: FUNCIONARIOS / CATEGORÍAS / RESERVAS ===============
    // =============== C: RECURSOS / ACTIVIDADES / ESTADÍSTICAS ===============

    @Override
    public void stop() {
        try {
            Database.instance().close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}