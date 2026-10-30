package reservas.presentation.reserva;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Reserva;
import reservas.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {

    public static final String CURRENT = "current";
    public static final String LIST = "list";
    public static final String CATEGORIAS = "categorias";

    private Reserva current;
    private List<Reserva> reservas;
    private List<CategoriaRecurso> categorias;
    private List<CategoriaRecurso> seleccionadas;

    public Model() {
        current = new Reserva();
        reservas = new ArrayList<>();
        categorias = new ArrayList<>();
        seleccionadas = new ArrayList<>();
    }

    public Reserva getCurrent() {
        return current;
    }

    public void setCurrent(Reserva current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
        firePropertyChange(LIST);
    }

    public List<CategoriaRecurso> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<CategoriaRecurso> categorias) {
        this.categorias = categorias;
        firePropertyChange(CATEGORIAS);
    }

    public List<CategoriaRecurso> getSeleccionadas() {
        return seleccionadas;
    }

    public void setSeleccionadas(
            List<CategoriaRecurso> seleccionadas) {
        this.seleccionadas = seleccionadas;
    }

    @Override
    public void addPropertyChangeListener(
            PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(CATEGORIAS);
        firePropertyChange(CURRENT);
        firePropertyChange(LIST);
    }
}
