package reservas.logic;

import java.io.Serializable;
import java.util.Objects;

public class CategoriaRecurso implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String descripcion;

    public CategoriaRecurso() { }
    public CategoriaRecurso(String id, String descripcion) { this.id = id; this.descripcion = descripcion; }

    public String getID() { return id; }
    public void setID(String id) { this.id = id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoriaRecurso)) return false;
        CategoriaRecurso other = (CategoriaRecurso) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override public String toString() { return descripcion; }
}