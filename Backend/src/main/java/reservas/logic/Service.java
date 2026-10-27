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
import java.time.LocalTime;
import java.time.LocalDate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import java.util.stream.Collectors;

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
    static void validar(Funcionario f) throws Exception {
        if (f == null) throw new Exception("Funcionario inválido");
        if (vacio(f.getId())) throw new Exception("El id es requerido");
        if (vacio(f.getNombre())) throw new Exception("El nombre es requerido");
        if (vacio(f.getTelefono())) throw new Exception("El teléfono es requerido");
        if (!f.getTelefono().trim().matches("[0-9 +()-]{4,20}"))
            throw new Exception("El teléfono solo puede tener números (mínimo 4)");
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
    static void validar(CategoriaRecurso c) throws Exception {
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
    static void validar(Recurso r) throws Exception {
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

    static List<CategoriaRecurso> validar(Reserva r, List<CategoriaRecurso> categorias,
                                          LocalDate hoy, LocalTime ahora) throws Exception {
        if (r == null) throw new Exception("Reserva inválida");
        if (r.getFuncionario() == null || vacio(r.getFuncionario().getId()))
            throw new Exception("La reserva no tiene un funcionario asociado");
        if (vacio(r.getDescripcion())) throw new Exception("Debe indicar la actividad");
        if (r.getDescripcion().trim().length() > 150)
            throw new Exception("La actividad no puede tener más de 150 caracteres");
        if (r.getFecha() == null) throw new Exception("Debe indicar la fecha");
        if (r.getDesde() == null || r.getHasta() == null)
            throw new Exception("Debe indicar la hora de inicio y la hora final");
        if (!r.getDesde().isBefore(r.getHasta()))
            throw new Exception("La hora de fin debe ser mayor a la hora de inicio");
        if (r.getFecha().isBefore(hoy))
            throw new Exception("No se pueden hacer reservas en fechas pasadas");
        if (r.getFecha().equals(hoy) && r.getDesde().isBefore(ahora.withSecond(0).withNano(0)))
            throw new Exception("No se pueden hacer reservas en horas que ya pasaron");
        if (categorias == null || categorias.isEmpty())
            throw new Exception("Debe seleccionar al menos una categoria");

        // Elimina categorias repetidas (por id) conservando el orden
        Map<String, CategoriaRecurso> unicas = new LinkedHashMap<>();
        for (CategoriaRecurso c : categorias) {
            if (c == null || vacio(c.getID())) throw new Exception("Categoría inválida");
            unicas.put(c.getID(), c);
        }
        r.setDescripcion(r.getDescripcion().trim());
        return new ArrayList<>(unicas.values());
    }

    @Override
    public synchronized Reserva createReserva(Reserva r, List<CategoriaRecurso> categorias) throws Exception {
        List<CategoriaRecurso> unicas = validar(r, categorias, LocalDate.now(), LocalTime.now());

        // Releer las categorias desde la BD: valida que existan y trae su descripcion real
        List<CategoriaRecurso> reales = new ArrayList<>();
        for (CategoriaRecurso c : unicas) reales.add(categoriaDao.read(c.getID()));

        Funcionario f = funcionarioDao.read(r.getFuncionario().getId());
        f.setClave("");
        r.setFuncionario(f);

        return reservaDao.create(r, reales);   // transaccion + asignacion de recursos libres
    }

    @Override
    public synchronized void deleteReserva(int id) throws Exception {
        Reserva r = reservaDao.read(id);
        if (r.getFecha().isBefore(LocalDate.now()))
            throw new Exception("Solo se pueden cancelar reservas futuras");
        reservaDao.delete(id);
    }

    @Override
    public synchronized List<Reserva> findAllReservas() throws Exception {
        return reservaDao.findAll();
    }

    @Override
    public synchronized Reserva findReservaById(int id) throws Exception {
        return reservaDao.read(id);
    }

    @Override
    public synchronized List<Reserva> findReservasByFuncionario(String idFuncionario) throws Exception {
        if (vacio(idFuncionario)) throw new Exception("Debe indicar el funcionario");
        return reservaDao.findByFuncionario(idFuncionario.trim());
    }

    static void validarRango(LocalDate desde, LocalDate hasta) throws Exception {
        if (desde == null || hasta == null) throw new Exception("Debe indicar las fechas desde y hasta");
        if (hasta.isBefore(desde)) throw new Exception("La fecha hasta debe ser posterior a la fecha desde");
    }

    @Override
    public synchronized List<Reserva> searchReservas(LocalDate desde, LocalDate hasta) throws Exception {
        validarRango(desde, hasta);
        return reservaDao.search(desde, hasta);
    }

    // =============== IA ===============
    // No es synchronized completo: la llamada al modelo puede tardar y no debe bloquear a los demas clientes.
    @Override
    public ReservaExtraccion extraerReserva(String frase) throws Exception {
        if (vacio(frase)) throw new Exception("Escriba una frase para interpretar");

        String listaCategorias;
        synchronized (this) {
            listaCategorias = categoriaDao.search("").stream()
                    .map(CategoriaRecurso::getDescripcion)
                    .collect(Collectors.joining("\n"));
        }

        try {
            OpenAiChatModel aiModel = OpenAiChatModel.builder()
                    .baseUrl("http://langchain4j.dev/demo/openai/v1")
                    .apiKey("demo")
                    .modelName("gpt-4o-mini")
                    .build();
            ReservaExtractorService aiService = AiServices.create(ReservaExtractorService.class, aiModel);
            ReservaExtraccion datos = aiService.extraer(frase, listaCategorias, LocalDate.now().toString());
            if (datos == null) throw new Exception("La IA no devolvió datos");
            return datos;
        } catch (Exception ex) {
            throw new Exception("No se pudo interpretar la frase: " + ex.getMessage());
        }
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