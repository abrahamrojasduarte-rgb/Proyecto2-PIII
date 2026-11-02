package reservas.presentation.estadisticas;

import reservas.logic.Service;
import reservas.presentation.util.PDFReportGenerator;
import java.time.LocalDate;

public class Controller {
    private final Model model;
    private final viewEstadisticas view;
    public Controller(Model model, viewEstadisticas view) {
        this.model = model;
        this.view = view;
        view.setModel(model);
        view.setController(this);
    }
    private void validar(LocalDate desde, LocalDate hasta) throws Exception {
        if (desde == null || hasta == null) throw new Exception("Debe indicar las fechas desde y hasta");
        if (hasta.isBefore(desde)) throw new Exception("La fecha hasta debe ser posterior a la fecha desde");
    }
    public void cargarRecursos(LocalDate desde, LocalDate hasta) throws Exception {
        validar(desde, hasta);
        model.setRecursos(Estadisticas.recursosPorCategoria(
                Service.instance().searchReservas(desde, hasta), desde, hasta));
    }
    public void cargarActividades(LocalDate desde, LocalDate hasta) throws Exception {
        validar(desde, hasta);
        model.setActividades(Estadisticas.actividadesPorSemana(
                Service.instance().searchReservas(desde, hasta), desde, hasta));
    }
    public void imprimirRecursos(String ruta) throws Exception {
        if (model.getRecursos().isEmpty()) throw new Exception("Debe cargar las estadisticas de recursos");
        PDFReportGenerator.generarReporteMatriz("Recursos reservados", new String[]{"Categoria","Cantidad"}, model.getRecursos(), ruta);
    }
    public void imprimirActividades(String ruta) throws Exception {
        if (model.getActividades().isEmpty()) throw new Exception("Debe cargar las estadisticas de actividades");
        PDFReportGenerator.generarReporteMatriz("Actividades programadas", new String[]{"Semana", "Cantidad"}, model.getActividades(), ruta);
    }
}
