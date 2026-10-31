package reservas.presentation.calendarizacion;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.presentation.AbstractModel;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    public static final String CATEGORIAS = "categorias";
    public static final String MATRIZ = "matriz";
    public static final int HORA_INICIO = 6;
    public static final int HORA_FIN = 21;
    private LocalDate fecha;
    private List<CategoriaRecurso> categorias;
    private List<Recurso> recursos;
    private List<Reserva> reservas;
    public Model() {
        fecha = LocalDate.now();
        categorias = new ArrayList<>();
        recursos = new ArrayList<>();
        reservas = new ArrayList<>();
    }
    public LocalDate getFecha() { return fecha; }
    public List<CategoriaRecurso> getCategorias() { return categorias; }
    public void setCategorias(List<CategoriaRecurso> categorias) {
        this.categorias = categorias;
        firePropertyChange(CATEGORIAS);
    }
    public List<Recurso> getRecursos() { return recursos; }
    public List<Reserva> getReservas() { return reservas; }
    public void setMatriz(LocalDate fecha, List<Recurso> recursos, List<Reserva> reservas) {
        this.fecha = fecha;
        this.recursos = recursos;
        this.reservas = reservas;
        firePropertyChange(MATRIZ);
    }
    public String buscarTexto(Recurso recurso, int hora) {
        for (Reserva reserva : reservas) {
            if (!reserva.getRecursos().contains(recurso)) continue; // equals por id
            if (reserva.ocupaHora(hora)) {
                return reserva.getDescripcion() + " - " + reserva.getFuncionario().getNombre();
            }
        }
        return "";
    }
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(CATEGORIAS);
        firePropertyChange(MATRIZ);
    }
}
