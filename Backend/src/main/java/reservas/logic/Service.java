package reservas.logic;

import reservas.data.CategoriaDao;
import reservas.data.Database;
import reservas.data.FuncionarioDao;
import reservas.data.RecursoDao;
import reservas.data.ReservaDao;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Service implements IService {
    private static Service theInstance;

    public static synchronized IService instance() {
        if (theInstance == null) theInstance = new Service();
        return theInstance;
    }

    private final FuncionarioDao funcionarioDao;
    private final CategoriaDao categoriaDao;
    private final RecursoDao recursoDao;
    private final ReservaDao reservaDao;

    Service() {
        funcionarioDao = new FuncionarioDao();
        categoriaDao = new CategoriaDao();
        recursoDao = new RecursoDao();
        reservaDao = new ReservaDao();
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    // =============== LOGIN ===============
    @Override
    public synchronized Funcionario login(Funcionario f) throws Exception {
        if (f == null || vacio(f.getId())) throw new Exception("Debe indicar el usuario");
        if (vacio(f.getClave())) throw new Exception("Debe indicar la clave");

        Funcionario real = funcionarioDao.read(f.getId().trim());
        if (!real.getClave().equals(f.getClave())) throw new Exception("Clave incorrecta");

        real.setClave("");   // la clave no viaja de regreso al Frontend
        return real;
    }

    @Override
    public synchronized void cambiarClave(Funcionario f, String claveNueva) throws Exception {
        if (f == null || vacio(f.getId())) throw new Exception("Debe indicar el usuario");
        if (vacio(f.getClave())) throw new Exception("Debe indicar la clave actual");
        if (vacio(claveNueva)) throw new Exception("La clave nueva no puede estar vacía");
        if (claveNueva.length() < 3) throw new Exception("La clave nueva debe tener al menos 3 caracteres");

        Funcionario real = funcionarioDao.read(f.getId().trim());
        if (!real.getClave().equals(f.getClave())) throw new Exception("La clave actual es incorrecta");

        funcionarioDao.updateClave(real.getId(), claveNueva);
    }

    // =============== FUNCIONARIOS ===============
    private void validar(Funcionario f) throws Exception {
        if (f == null) throw new Exception("Funcionario inválido");
        if (vacio(f.getId())) throw new Exception("El id es requerido");
        if (vacio(f.getNombre())) throw new Exception("El nombre es requerido");
        if (vacio(f.getTelefono())) throw new Exception("El teléfono es requerido");
        f.setId(f.getId().trim());
        f.setNombre(f.getNombre().trim());
        f.setTelefono(f.getTelefono().trim());
    }

    @Override
    public synchronized void create(Funcionario f) throws Exception {
        validar(f);
        f.setClave(f.getId());            // clave inicial = id
        f.setRol(Rol.FUNCIONARIO);
        funcionarioDao.create(f);
    }

    @Override
    public synchronized void update(Funcionario f) throws Exception {
        validar(f);
        funcionarioDao.update(f);
    }

    @Override
    public synchronized void delete(String id) throws Exception {
        if (vacio(id)) throw new Exception("Debe indicar el id del funcionario");
        funcionarioDao.delete(id.trim());
    }

    @Override
    public synchronized List<Funcionario> findAll() throws Exception {
        return search("", "");
    }

    @Override
    public synchronized List<Funcionario> search(String id, String nombre) throws Exception {
        List<Funcionario> lista = funcionarioDao.search(id, nombre);
        for (Funcionario f : lista) f.setClave("");   // nunca enviar claves
        return lista;
    }

    // =============== CATEGORIAS ===============
    private void validar(CategoriaRecurso c) throws Exception {
        if (c == null || vacio(c.getDescripcion()))
            throw new Exception("La descripción de la categoría no puede estar vacía");
        c.setDescripcion(c.getDescripcion().trim());
    }

    @Override
    public synchronized CategoriaRecurso createCategoria(CategoriaRecurso c) throws Exception {
        validar(c);
        return categoriaDao.create(c);
    }

    @Override
    public synchronized void updateCategoria(CategoriaRecurso c) throws Exception {
        validar(c);
        if (vacio(c.getID())) throw new Exception("Debe seleccionar una categoría");
        categoriaDao.update(c);
    }

    @Override
    public synchronized void deleteCategoria(String id) throws Exception {
        if (vacio(id)) throw new Exception("Debe seleccionar una categoría válida para borrar");
        categoriaDao.delete(id);
    }

    @Override
    public synchronized List<CategoriaRecurso> findAllCategorias() throws Exception {
        return categoriaDao.search("");
    }

    @Override
    public synchronized List<CategoriaRecurso> searchCategorias(String descripcion) throws Exception {
        return categoriaDao.search(descripcion);
    }

    // =============== RECURSOS ===============
    private void validar(Recurso r) throws Exception {
        if (r == null) throw new Exception("Recurso inválido");
        if (vacio(r.getId())) throw new Exception("El id (número de activo) es requerido");
        if (vacio(r.getDescripcion())) throw new Exception("La descripción es requerida");
        if (r.getCategoria() == null || vacio(r.getCategoria().getID()))
            throw new Exception("Debe seleccionar una categoría");
        r.setId(r.getId().trim());
        r.setDescripcion(r.getDescripcion().trim());
    }

    @Override
    public synchronized void createRecurso(Recurso r) throws Exception {
        validar(r);
        recursoDao.create(r);
    }

    @Override
    public synchronized void updateRecurso(Recurso r) throws Exception {
        validar(r);
        recursoDao.update(r);
    }

    @Override
    public synchronized void deleteRecurso(String id) throws Exception {
        if (vacio(id)) throw new Exception("Debe indicar el id del recurso");
        recursoDao.delete(id.trim());
    }

    @Override
    public synchronized List<Recurso> findAllRecursos() throws Exception {
        return recursoDao.search("", "");
    }

    @Override
    public synchronized List<Recurso> searchRecursos(String categoriaId, String descripcion) throws Exception {
        return recursoDao.search(categoriaId, descripcion);
    }

    // =============== RESERVAS ===============
    @Override
    public synchronized Reserva createReserva(Reserva r) throws Exception {
        if (r == null) throw new Exception("Reserva inválida");
        if (r.getFuncionario() == null || vacio(r.getFuncionario().getId()))
            throw new Exception("La reserva no tiene un funcionario asociado");
        if (vacio(r.getDescripcion())) throw new Exception("Debe indicar la descripción de la reserva");
        if (r.getFecha() == null) throw new Exception("Debe indicar la fecha");
        if (r.getDesde() == null || r.getHasta() == null)
            throw new Exception("Debe indicar la hora de inicio y la hora final");
        if (!r.getDesde().isBefore(r.getHasta()))
            throw new Exception("La hora de inicio debe ser anterior a la hora final");
        if (r.getRecursos() == null || r.getRecursos().isEmpty())
            throw new Exception("Debe seleccionar al menos un recurso");

        // Releer los recursos desde la BD: valida que existan y elimina duplicados
        Map<String, Recurso> unicos = new LinkedHashMap<>();
        for (Recurso x : r.getRecursos()) {
            Recurso real = recursoDao.read(x.getId());
            unicos.put(real.getId(), real);
        }
        r.setRecursos(new ArrayList<>(unicos.values()));

        Funcionario f = funcionarioDao.read(r.getFuncionario().getId());
        f.setClave("");
        r.setFuncionario(f);
        r.setDescripcion(r.getDescripcion().trim());

        return reservaDao.create(r);   // transacción + chequeo de traslapes
    }

    @Override
    public synchronized List<Reserva> findAllReservas() throws Exception {
        return reservaDao.findAll();
    }

    @Override
    public void stop() {
        try {
            Database.instance().close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}