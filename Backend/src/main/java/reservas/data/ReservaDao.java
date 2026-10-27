package reservas.data;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaDao {
    Database db = Database.instance();

    private static final String SELECT =
            "select r.id as r_id, r.descripcion as r_desc, r.fecha, r.desde, r.hasta, " +
                    "f.id as f_id, f.nombre as f_nombre, f.telefono as f_tel " +
                    "from reserva r inner join funcionario f on r.funcionario_id = f.id ";

    private static final String ORDER = " order by r.fecha, r.desde, r.id";


     //Crea la reserva asignando un recurso libre de cada categoria pedida.
     //todito ocurre en una sola transaccion: si alguna categoria no tiene recursos
     //libres en ese horario, no se guarda nada.

    public Reserva create(Reserva r, List<CategoriaRecurso> categorias) throws Exception {
        Connection cnx = db.connection();   // misma conexion: la transaccion debe ser una sola
        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            List<Recurso> asignados = new ArrayList<>();
            List<String> sinDisponibilidad = new ArrayList<>();
            for (CategoriaRecurso c : categorias) {
                Recurso libre = buscarLibre(cnx, c, r);
                if (libre == null) sinDisponibilidad.add(c.getDescripcion());
                else asignados.add(libre);
            }
            if (!sinDisponibilidad.isEmpty()) {
                throw new Exception("No hay disponibilidad en: " + String.join(", ", sinDisponibilidad));
            }
            r.setRecursos(asignados);

            String sql = "insert into reserva (funcionario_id, descripcion, fecha, desde, hasta) values (?,?,?,?,?)";
            try (PreparedStatement stm = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stm.setString(1, r.getFuncionario().getId());
                stm.setString(2, r.getDescripcion());
                stm.setDate(3, Date.valueOf(r.getFecha()));
                stm.setTime(4, Time.valueOf(r.getDesde()));
                stm.setTime(5, Time.valueOf(r.getHasta()));
                stm.executeUpdate();
                try (ResultSet keys = stm.getGeneratedKeys()) {
                    if (keys.next()) r.setId(keys.getInt(1));
                }
            }

            try (PreparedStatement stm = cnx.prepareStatement(
                    "insert into reserva_recurso (reserva_id, recurso_id) values (?,?)")) {
                for (Recurso rec : asignados) {
                    stm.setInt(1, r.getId());
                    stm.setString(2, rec.getId());
                    stm.addBatch();
                }
                stm.executeBatch();
            }
            cnx.commit();
            return r;
        } catch (Exception ex) {
            cnx.rollback();
            throw ex;
        } finally {
            cnx.setAutoCommit(autoCommit);
        }
    }

    private Recurso buscarLibre(Connection cnx, CategoriaRecurso c, Reserva r) throws Exception {
        String sql = RecursoDao.SELECT +
                "where r.categoria_id = ? and not exists (" +
                "  select 1 from reserva_recurso rr inner join reserva x on x.id = rr.reserva_id " +
                "  where rr.recurso_id = r.id and x.fecha = ? and x.desde < ? and x.hasta > ?) " +
                "order by r.id limit 1";
        try (PreparedStatement stm = cnx.prepareStatement(sql)) {
            stm.setString(1, c.getID());
            stm.setDate(2, Date.valueOf(r.getFecha()));
            stm.setTime(3, Time.valueOf(r.getHasta()));
            stm.setTime(4, Time.valueOf(r.getDesde()));
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? RecursoDao.from(rs) : null;
            }
        }
    }

    public Reserva read(int id) throws Exception {
        List<Reserva> lista = query(SELECT + "where r.id = ?", stm -> stm.setInt(1, id));
        if (lista.isEmpty()) throw new Exception("La reserva no existe");
        return lista.get(0);
    }

    public void delete(int id) throws Exception {
        // reserva_recurso se borra en cascada (ON DELETE CASCADE)
        try (PreparedStatement stm = db.prepareStatement("delete from reserva where id = ?")) {
            stm.setInt(1, id);
            if (stm.executeUpdate() == 0) throw new Exception("La reserva no existe");
        }
    }

    public List<Reserva> findAll() throws Exception {
        return query(SELECT + ORDER, stm -> { });
    }

    public List<Reserva> findByFuncionario(String funcionarioId) throws Exception {
        return query(SELECT + "where r.funcionario_id = ?" + ORDER, stm -> stm.setString(1, funcionarioId));
    }

    public List<Reserva> search(LocalDate desde, LocalDate hasta) throws Exception {
        return query(SELECT + "where r.fecha between ? and ?" + ORDER, stm -> {
            stm.setDate(1, Date.valueOf(desde));
            stm.setDate(2, Date.valueOf(hasta));
        });
    }

    private interface Parametros {
        void aplicar(PreparedStatement stm) throws Exception;
    }

    private List<Reserva> query(String sql, Parametros parametros) throws Exception {
        List<Reserva> lista = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            parametros.aplicar(stm);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) lista.add(from(rs));
            }
        }
        for (Reserva r : lista) r.setRecursos(recursosDe(r.getId()));
        return lista;
    }

    private static Reserva from(ResultSet rs) throws Exception {
        Reserva r = new Reserva();
        r.setId(rs.getInt("r_id"));
        r.setDescripcion(rs.getString("r_desc"));
        r.setFecha(rs.getDate("fecha").toLocalDate());
        r.setDesde(rs.getTime("desde").toLocalTime());
        r.setHasta(rs.getTime("hasta").toLocalTime());
        Funcionario f = new Funcionario();
        f.setId(rs.getString("f_id"));
        f.setNombre(rs.getString("f_nombre"));
        f.setTelefono(rs.getString("f_tel"));
        f.setClave("");                       // nunca viaja la clave
        r.setFuncionario(f);
        return r;
    }

    private List<Recurso> recursosDe(int reservaId) throws Exception {
        String sql = RecursoDao.SELECT +
                "inner join reserva_recurso rr on rr.recurso_id = r.id where rr.reserva_id = ? order by r.id";
        List<Recurso> lista = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, reservaId);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) lista.add(RecursoDao.from(rs));
            }
        }
        return lista;
    }
}