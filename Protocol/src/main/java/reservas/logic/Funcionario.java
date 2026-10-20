package reservas.logic;

import java.io.Serializable;
import java.util.Objects;

public class Funcionario implements Serializable {
    public static final String ADMINISTRADOR = "ADMINISTRADOR";
    public static final String FUNCIONARIO = "FUNCIONARIO";

    private String id;
    private String nombre;
    private String clave;
    private String rol;

    public Funcionario(String id, String nombre, String clave, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.clave = clave;
        this.rol = rol;
    }

    public Funcionario() {
        this("", "", "", FUNCIONARIO);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    @Override
    public String toString() { return nombre; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Funcionario) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}