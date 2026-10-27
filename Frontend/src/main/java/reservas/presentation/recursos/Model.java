package reservas.presentation.recursos;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    private Recurso current;
    private boolean editando;          // true si current ya existe (se selecciono de la tabla)
    private List<Recurso> recursos;
    private List<CategoriaRecurso> categorias;

    public static final String CURRENT = "current";
    public static final String LIST = "list";
    public static final String CATEGORIAS = "categorias";

    public Model() {
        current = new Recurso();
        recursos = new ArrayList<>();
        categorias = new ArrayList<>();
    }

    public Recurso getCurrent() { return current; }
    public boolean isEditando() { return editando; }
    public void setCurrent(Recurso current, boolean editando) {
        this.current = current;
        this.editando = editando;
        firePropertyChange(CURRENT);
    }

    public List<Recurso> getRecursos() { return recursos; }
    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
        firePropertyChange(LIST);
    }

    public List<CategoriaRecurso> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaRecurso> categorias) {
        this.categorias = categorias;
        firePropertyChange(CATEGORIAS);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(CATEGORIAS);
        firePropertyChange(CURRENT);
        firePropertyChange(LIST);
    }
}
