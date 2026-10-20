package reservas.data;

import reservas.logic.Funcionario;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FuncionarioDao {
    Database db;

    public FuncionarioDao() {
        db = Database.instance();
    }

    public Funcionario read(String id) throws Exception {
        String sql = "select * from funcionario f where f.id=?";
        PreparedStatement stm = db.prepareStatement(sql);
        stm.setString(1, id);
        ResultSet rs = db.executeQuery(stm);
        if (rs != null && rs.next()) {
            return from(rs, "f");
        } else {
            throw new Exception("Funcionario no existe");
        }
    }

    public void updateClave(String id, String claveNueva) throws Exception {
        String sql = "update funcionario set clave=? where id=?";
        PreparedStatement stm = db.prepareStatement(sql);
        stm.setString(1, claveNueva);
        stm.setString(2, id);
        int count = db.executeUpdate(stm);
        if (count == 0) {
            throw new Exception("Funcionario no existe");
        }
    }

    public Funcionario from(ResultSet rs, String alias) {
        try {
            Funcionario f = new Funcionario();
            f.setId(rs.getString(alias + ".id"));
            f.setNombre(rs.getString(alias + ".nombre"));
            f.setClave(rs.getString(alias + ".clave"));
            f.setRol(rs.getString(alias + ".rol"));
            return f;
        } catch (SQLException ex) {
            return null;
        }
    }
}