package reservas.presentation.calendarizacion;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Service;
import reservas.presentation.util.PDFReportGenerator;
import java.time.LocalDate;
import java.util.List;
public class Controller {
    private final Model model;
    private final viewCalendarizacion view;
    public Controller(Model model, viewCalendarizacion view) {
        this.model = model;
        this.view = view;
        view.setModel(model);
        view.setController(this);
    }
    public void refresh() throws Exception {
        model.setCategorias(Service.instance().findAllCategorias());
    }
    public void cargar(LocalDate fecha, CategoriaRecurso categoria) throws Exception {
        if (fecha == null) throw new Exception("Debe seleccionar una fecha");
        if (categoria == null) throw new Exception("Debe seleccionar una categoria");
        model.setMatriz(fecha,
                Service.instance().searchRecursos(categoria.getID(), ""),
                Service.instance().searchReservas(fecha, fecha));
    }
    public void imprimir(String ruta, String[] columnas, List<String[]> filas) throws Exception {
        if (model.getRecursos().isEmpty()) throw new Exception("Debe cargar la calendarizacion");
        PDFReportGenerator.generarReporteMatriz("Calendarizacion de Recursos - " + model.getFecha(),
                columnas, filas, ruta);
    }
}
