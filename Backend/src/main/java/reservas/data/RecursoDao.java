package reservas.data;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class RecursoDao {
    Database db = Database.instance();

    static final String SELECT =
            "select r.id as r_id, r.descripcion as r_desc, c.id as c_id, c.descripcion as c_desc " +
                    "from recurso r inner join categoria c on r.categoria_id = c.id ";

    public static Recurso from(ResultSet rs) throws SQLException {
        return new Recurso(rs.getString("r_id"), rs.getString("r_desc"),
                new CategoriaRecurso(rs.getString("c_id"), rs.getString("c_desc")));
    }

    public void create(Recurso r) throws Exception {
        String sql = "insert into recurso (id, descripcion, categoria_id) values (?,?,?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, r.getId());
            stm.setString(2, r.getDescripcion());
            stm.setString(3, r.getCategoria().getID());
            stm.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            if (ex.getErrorCode() == 1062) throw new Exception("Ya existe un recurso con ese id");
            throw new Exception("La categoría seleccionada no existe");
        }
    }

    public Recurso read(String id) throws Exception {
        try (PreparedStatement stm = db.prepareStatement(SELECT + "where r.id = ?")) {
            stm.setString(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) return from(rs);
            }
        }
        throw new Exception("El recurso " + id + " no existe");
    }

    public void update(Recurso r) throws Exception {
        String sql = "update recurso set descripcion=?, categoria_id=? where id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, r.getDescripcion());
            stm.setString(2, r.getCategoria().getID());
            stm.setString(3, r.getId());
            if (stm.executeUpdate() == 0) throw new Exception("Recurso no existe");
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("La categoría seleccionada no existe");
        }
    }

    public void delete(String id) throws Exception {
        String sql = "delete from recurso where id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id);
            if (stm.executeUpdate() == 0) throw new Exception("Recurso no existe");
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("No se puede borrar: el recurso tiene reservas registradas");
        }
    }

    // categoriaId vacío o null = todas las categorías
    public List<Recurso> search(String categoriaId, String descripcion) throws Exception {
        String sql = SELECT + "where (? = '' or r.categoria_id = ?) and r.descripcion like ? order by r.id";
        String cat = categoriaId == null ? "" : categoriaId;
        List<Recurso> lista = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, cat);
            stm.setString(2, cat);
            stm.setString(3, "%" + (descripcion == null ? "" : descripcion.trim()) + "%");
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) lista.add(from(rs));
            }
        }
        return lista;
    }
}