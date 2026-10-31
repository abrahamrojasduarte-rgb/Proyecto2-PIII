package reservas.presentation.actividades;

import reservas.logic.Service;
import reservas.presentation.util.PDFReportGenerator;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
public class Controller {
    private final Model model;
    private final viewActividades view;
    public Controller(Model model, viewActividades view) {
        this.model = model;
        this.view = view;
        view.setModel(model);
        view.setController(this);
    }
    /** Carga la semana (lunes a domingo) que contiene la fecha de referencia. */
    public void cargar(LocalDate referencia) throws Exception {
        if (referencia == null) throw new Exception("Debe seleccionar una fecha de referencia");
        LocalDate lunes = referencia.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);
        model.setSemana(lunes, Service.instance().searchReservas(lunes, domingo));
    }
    public void imprimir(String ruta, String[] columnas, List<String[]> filas) throws Exception {
        PDFReportGenerator.generarReporteMatriz("Programacion de Actividades - Semana del " +
                        model.getLunes(),
                columnas, filas, ruta);
    }
}
