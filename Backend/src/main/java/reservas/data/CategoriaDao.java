package reservas.data;

import reservas.logic.CategoriaRecurso;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDao {
    Database db = Database.instance();

    private String nextId() throws SQLException {
        String sql = "select coalesce(max(cast(substring(id, 5) as unsigned)), 0) from categoria";
        try (PreparedStatement stm = db.prepareStatement(sql); ResultSet rs = stm.executeQuery()) {
            rs.next();
            return String.format("CAT-%06d", rs.getInt(1) + 1);
        }
    }

    public CategoriaRecurso create(CategoriaRecurso c) throws Exception {
        c.setID(nextId());
        String sql = "insert into categoria (id, descripcion) values (?,?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, c.getID());
            stm.setString(2, c.getDescripcion());
            stm.executeUpdate();
        }
        return c;
    }

    public void update(CategoriaRecurso c) throws Exception {
        String sql = "update categoria set descripcion=? where id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, c.getDescripcion());
            stm.setString(2, c.getID());
            if (stm.executeUpdate() == 0) throw new Exception("Categoría no existe");
        }
    }

    public void delete(String id) throws Exception {
        String sql = "delete from categoria where id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id);
            if (stm.executeUpdate() == 0) throw new Exception("Categoría no existe");
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("No se puede borrar: la categoría tiene recursos asociados");
        }
    }

    public List<CategoriaRecurso> search(String descripcion) throws Exception {
        String sql = "select * from categoria where descripcion like ? order by id";
        List<CategoriaRecurso> lista = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, "%" + (descripcion == null ? "" : descripcion.trim()) + "%");
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    lista.add(new CategoriaRecurso(rs.getString("id"), rs.getString("descripcion")));
                }
            }
        }
        return lista;
    }

    public CategoriaRecurso read(String id) throws Exception{
        String sql = "select * from categoria where id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id);
            try(ResultSet rs = stm.executeQuery()) {
                if (rs.next()){
                    return new CategoriaRecurso(rs.getString("id"), rs.getString("descripcion"));
                }
            }
        }
        throw new Exception("La categoria " + id + " no existe");
    }

}