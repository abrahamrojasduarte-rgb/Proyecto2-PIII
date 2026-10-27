package reservas.logic;

import java.time.LocalDate;
import java.util.List;

public interface IService {
    // =============== LOGIN ===============
    Funcionario login(Funcionario f) throws Exception;
    void cambiarClave(Funcionario f, String claveNueva) throws Exception;

    // =============== FUNCIONARIOS ===============
    void create(Funcionario f) throws Exception;
    void update(Funcionario f) throws Exception;
    void delete(String id) throws Exception;
    List<Funcionario> findAll() throws Exception;
    List<Funcionario> search(String id, String nombre) throws Exception;

    // =============== CATEGORIAS ===============
    CategoriaRecurso createCategoria(CategoriaRecurso c) throws Exception;
    void updateCategoria(CategoriaRecurso c) throws Exception;
    void deleteCategoria(String id) throws Exception;
    List<CategoriaRecurso> findAllCategorias() throws Exception;
    List<CategoriaRecurso> searchCategorias(String descripcion) throws Exception;

    // =============== RECURSOS ===============
    void createRecurso(Recurso r) throws Exception;
    void updateRecurso(Recurso r) throws Exception;
    void deleteRecurso(String id) throws Exception;
    List<Recurso> findAllRecursos() throws Exception;
    List<Recurso> searchRecursos(String categoriaId, String descripcion) throws Exception;

    // =============== RESERVAS ===============
    // Crea una reserva pidiendo una unidad de cada categoria. El Backend asigna,
    // dentro de una transaccion, un recurso libre de cada categoria en ese horario.

    Reserva createReserva(Reserva r, List<CategoriaRecurso> categorias) throws Exception;
    void deleteReserva(int id) throws Exception;
    Reserva findReservaById(int id) throws Exception;
    List<Reserva> findAllReservas() throws Exception;
    List<Reserva> findReservasByFuncionario(String idFuncionario) throws Exception;
    // Reservas con fecha entre desde y hasta (ambas inclusive)
    List<Reserva> searchReservas(LocalDate desde, LocalDate hasta) throws Exception;

    // Extraccion con IA
    ReservaExtraccion extraerReserva(String frase) throws Exception;

    void stop();
}
