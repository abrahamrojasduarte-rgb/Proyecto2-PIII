package reservas.logic;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Notificacion asincronica que el Backend envia a los demas Frontend cuando se crea una reserva. */
public class AlertaReserva implements Serializable {
    private static final long serialVersionUID = 1L;

    private int reservaId;
    private String encargado;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime desde;
    private LocalTime hasta;
    private List<String> recursos;

    public AlertaReserva(String encargado, String descripcion, LocalDate fecha,
                         LocalTime desde, LocalTime hasta, List<String> recursos) {
        this(0, encargado, descripcion, fecha, desde, hasta, recursos);
    }

    public AlertaReserva(int reservaId, String encargado, String descripcion, LocalDate fecha,
                         LocalTime desde, LocalTime hasta, List<String> recursos) {
        this.reservaId = reservaId;
        this.encargado = encargado;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.desde = desde;
        this.hasta = hasta;
        this.recursos = recursos == null ? new ArrayList<>() : recursos;
    }

    /** Arma la alerta a partir de una reserva ya creada (recursos por descripcion). */
    public static AlertaReserva de(Reserva r) {
        List<String> recursos = new ArrayList<>();
        for (Recurso rec : r.getRecursos()) recursos.add(rec.getDescripcion());
        String encargado = r.getFuncionario() == null ? "" : r.getFuncionario().getNombre();
        return new AlertaReserva(r.getId(), encargado, r.getDescripcion(),
                r.getFecha(), r.getDesde(), r.getHasta(), recursos);
    }

    public int getReservaId() { return reservaId; }
    public String getEncargado() { return encargado; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getDesde() { return desde; }
    public LocalTime getHasta() { return hasta; }
    public List<String> getRecursos() { return recursos; }

    @Override
    public String toString() { return encargado + " - " + descripcion; }
}