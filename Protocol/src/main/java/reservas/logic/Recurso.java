package reservas.logic;

import java.io.Serializable;
import java.util.Objects;

public class Recurso implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String descripcion;
    private CategoriaRecurso categoria;

    public Recurso() { }
    public Recurso(String id, String descripcion, CategoriaRecurso categoria) {
        this.id = id; this.descripcion = descripcion; this.categoria = categoria;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public CategoriaRecurso getCategoria() { return categoria; }
    public void setCategoria(CategoriaRecurso categoria) { this.categoria = categoria; }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Recurso)) return false;

        Recurso other = (Recurso) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override public String toString(){
        return descripcion;
    }

}