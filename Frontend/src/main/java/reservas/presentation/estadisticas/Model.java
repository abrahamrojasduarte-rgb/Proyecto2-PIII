package reservas.presentation.estadisticas;

import reservas.presentation.AbstractModel;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    public static final String RECURSOS = "recursos";
    public static final String ACTIVIDADES = "actividades";

    private List<String[]> recursos;
    private List<String[]> actividades;

    public Model() {
        recursos = new ArrayList<>();
        actividades = new ArrayList<>();
    }
    public List<String[]> getRecursos() { return recursos; }
    public void setRecursos(List<String[]> recursos) {
        this.recursos = recursos;
        firePropertyChange(RECURSOS);
    }
    public List<String[]> getActividades() { return actividades; }
    public void setActividades(List<String[]> actividades) {
        this.actividades = actividades;
        firePropertyChange(ACTIVIDADES);
    }
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(RECURSOS);
        firePropertyChange(ACTIVIDADES);
    }
}
