package reservas.presentation.actividades;

import reservas.logic.Reserva;
import reservas.presentation.AbstractModel;
import java.beans.PropertyChangeListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    public static final String MATRIZ = "matriz";
    public static final int HORA_INICIO = 6;
    public static final int HORA_FIN = 21;
    public static final int DIAS = 7;
    private LocalDate lunes;
    private List<Reserva> reservas;
    public Model() {
        lunes = LocalDate.now().with(DayOfWeek.MONDAY);
        reservas = new ArrayList<>();
    }
    public LocalDate getLunes() { return lunes; }
    public LocalDate getDia(int indice) { return lunes.plusDays(indice); }
    public List<Reserva> getReservas() { return reservas; }
    /** Fija la semana y sus reservas, y redibuja la matriz. */
    public void setSemana(LocalDate lunes, List<Reserva> reservas) {
        this.lunes = lunes;
        this.reservas = reservas;
        firePropertyChange(MATRIZ);
    }
    public String buscarTexto(LocalDate dia, int hora) {
        StringBuilder texto = new StringBuilder();
        for (Reserva reserva : reservas) {
            if (!reserva.getFecha().equals(dia) || !reserva.ocupaHora(hora)) continue;
            if (texto.length() > 0) texto.append(" | ");
            texto.append(reserva.getDescripcion()).append(" (").append(reserva.getFuncionario().getNombre()).append(")");
        }
        return texto.toString();
    }
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(MATRIZ);
    }
}
