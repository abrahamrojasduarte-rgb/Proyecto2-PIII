package reservas.data;

import reservas.logic.Funcionario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDao {
    Database db;

    public FuncionarioDao() {
        db = Database.instance();
    }

    private static String like(String s) {
        return "%" + (s == null ? "" : s.trim()) + "%";
    }

    public void create(Funcionario f) throws Exception {
        String sql = "insert into funcionario (id, nombre, telefono, clave, rol) values (?,?,?,?,?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, f.getId());
            stm.setString(2, f.getNombre());
            stm.setString(3, f.getTelefono());
            stm.setString(4, f.getClave());
            stm.setString(5, f.getRol());
            stm.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("Funcionario ya existe");
        }
    }

    public Funcionario read(String id) throws Exception {
        String sql = "select * from funcionario f where f.id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) return from(rs, "f");
            }
        }
        throw new Exception("Funcionario no existe");
    }

    public void update(Funcionario f) throws Exception {
        String sql = "update funcionario set nombre=?, telefono=? where id=? and rol='FUNCIONARIO'";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, f.getNombre());
            stm.setString(2, f.getTelefono());
            stm.setString(3, f.getId());
            if (stm.executeUpdate() == 0) throw new Exception("Funcionario no existe");
        }
    }

    public void updateClave(String id, String claveNueva) throws Exception {
        String sql = "update funcionario set clave=? where id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, claveNueva);
            stm.setString(2, id);
            if (stm.executeUpdate() == 0) throw new Exception("Funcionario no existe");
        }
    }

    public void delete(String id) throws Exception {
        String sql = "delete from funcionario where id=? and rol='FUNCIONARIO'";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id);
            if (stm.executeUpdate() == 0) throw new Exception("Funcionario no existe");
        } catch (SQLIntegrityConstraintViolationException ex) {
            throw new Exception("No se puede borrar: el funcionario tiene reservas registradas");
        }
    }

    public List<Funcionario> search(String id, String nombre) throws Exception {
        String sql = "select * from funcionario f where f.rol='FUNCIONARIO' " +
                "and f.id like ? and f.nombre like ? order by f.id";
        List<Funcionario> lista = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, like(id));
            stm.setString(2, like(nombre));
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) lista.add(from(rs, "f"));
            }
        }
        return lista;
    }

    public Funcionario from(ResultSet rs, String alias) throws SQLException {
        try{
            Funcionario f = new Funcionario();
            f.setId(rs.getString(alias + ".id"));
            f.setNombre(rs.getString(alias + ".nombre"));
            f.setTelefono(rs.getString(alias + ".telefono"));
            f.setClave(rs.getString(alias + ".clave"));
            f.setRol(rs.getString(alias + ".rol"));
        return f;
        } catch (SQLException ex) {
            return null;
        }
    }
}