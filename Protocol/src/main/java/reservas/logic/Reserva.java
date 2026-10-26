package reservas.logic;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Reserva implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private Funcionario funcionario;      // quien hizo la reserva
    private String descripcion;
    private LocalDate fecha;
    private LocalTime desde;
    private LocalTime hasta;
    private List<Recurso> recursos = new ArrayList<>();

    public Reserva() {}
    public Reserva(String descripcion, LocalDate fecha, LocalTime desde, LocalTime hasta){
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.desde = desde;
        this.hasta = hasta;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getDesde() { return desde; }
    public void setDesde(LocalTime desde) { this.desde = desde; }
    public LocalTime getHasta() { return hasta; }
    public void setHasta(LocalTime hasta) { this.hasta = hasta; }
    public List<Recurso> getRecursos() { return recursos; }
    public void setRecursos(List<Recurso> recursos) { this.recursos = recursos; }

    public boolean ocupaHora(int hora){
        if (desde == null || hasta == null){
            return false;
        }
        LocalTime inicioBloque = LocalTime.of(hora, 0);
        LocalTime finBloque = hora == 23 ? LocalTime.MAX: LocalTime.of(hora + 1, 0);
        return desde.isBefore(finBloque) && hasta.isAfter(inicioBloque);
    }

    public boolean seTraslapa(LocalDate otraFecha, LocalTime otroDesde, LocalTime otroHasta){
        return fecha != null && fecha.equals(otraFecha) && desde.isBefore(otroHasta) && hasta.isAfter(otroDesde);
    }

}