package reservas.presentation.categorias;

import reservas.logic.CategoriaRecurso;
import reservas.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    private CategoriaRecurso current;
    private List<CategoriaRecurso> categorias;

    public static final String CURRENT = "current";
    public static final String LIST = "list";

    public Model() {
        current = new CategoriaRecurso();
        categorias = new ArrayList<>();
    }

    public CategoriaRecurso getCurrent() { return current; }
    public void setCurrent(CategoriaRecurso current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public List<CategoriaRecurso> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaRecurso> categorias) {
        this.categorias = categorias;
        firePropertyChange(LIST);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(CURRENT);
        firePropertyChange(LIST);
    }
}
