package reservas.data;

import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class ReservaDao {
    Database db = Database.instance();

    public Reserva create(Reserva r) throws Exception {
        Connection cnx = db.cnx;   // misma conexión: la transacción debe ser una sola
        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);
        try {
            for (Recurso rec : r.getRecursos()) verificarDisponible(cnx, rec, r);

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
                for (Recurso rec : r.getRecursos()) {
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

    // Dos reservas se traslapan si inicio1 < fin2 y fin1 > inicio2
    private void verificarDisponible(Connection cnx, Recurso rec, Reserva r) throws Exception {
        String sql = "select count(*) from reserva r inner join reserva_recurso rr on rr.reserva_id = r.id " +
                "where rr.recurso_id = ? and r.fecha = ? and r.desde < ? and r.hasta > ?";
        try (PreparedStatement stm = cnx.prepareStatement(sql)) {
            stm.setString(1, rec.getId());
            stm.setDate(2, Date.valueOf(r.getFecha()));
            stm.setTime(3, Time.valueOf(r.getHasta()));
            stm.setTime(4, Time.valueOf(r.getDesde()));
            try (ResultSet rs = stm.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0)
                    throw new Exception("El recurso '" + rec.getDescripcion() + "' ya está reservado en ese horario");
            }
        }
    }

    public List<Reserva> findAll() throws Exception {
        String sql = "select r.id as r_id, r.descripcion as r_desc, r.fecha, r.desde, r.hasta, " +
                "f.id as f_id, f.nombre as f_nombre " +
                "from reserva r inner join funcionario f on r.funcionario_id = f.id " +
                "order by r.fecha desc, r.desde";
        List<Reserva> lista = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql); ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                Reserva r = new Reserva();
                r.setId(rs.getInt("r_id"));
                r.setDescripcion(rs.getString("r_desc"));
                r.setFecha(rs.getDate("fecha").toLocalDate());
                r.setDesde(rs.getTime("desde").toLocalTime());
                r.setHasta(rs.getTime("hasta").toLocalTime());
                Funcionario f = new Funcionario();
                f.setId(rs.getString("f_id"));
                f.setNombre(rs.getString("f_nombre"));
                r.setFuncionario(f);
                lista.add(r);
            }
        }
        for (Reserva r : lista) r.setRecursos(recursosDe(r.getId()));
        return lista;
    }

    private List<Recurso> recursosDe(int reservaId) throws Exception {
        String sql = "select rec.id as r_id, rec.descripcion as r_desc, c.id as c_id, c.descripcion as c_desc " +
                "from reserva_recurso rr inner join recurso rec on rr.recurso_id = rec.id " +
                "inner join categoria c on rec.categoria_id = c.id where rr.reserva_id = ?";
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