package reservas.logic;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AlertaReserva implements Serializable {
    private String encargado;
    private String descripcion;
    private LocalDate fecha;
    private LocalTime desde;
    private LocalTime hasta;
    private List<String> recursos;

    public AlertaReserva(String encargado, String descripcion, LocalDate fecha,
                         LocalTime desde, LocalTime hasta, List<String> recursos) {
        this.encargado = encargado;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.desde = desde;
        this.hasta = hasta;
        this.recursos = recursos;
    }

    public String getEncargado() { return encargado; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getDesde() { return desde; }
    public LocalTime getHasta() { return hasta; }
    public List<String> getRecursos() { return recursos; }

    @Override
    public String toString() { return encargado + " - " + descripcion; }
}