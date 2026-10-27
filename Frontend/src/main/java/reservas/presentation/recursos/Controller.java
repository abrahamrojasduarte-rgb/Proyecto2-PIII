package reservas.presentation.recursos;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.logic.Service;
import reservas.presentation.util.PDFReportGenerator;

public class Controller {
    private final Model model;
    private final viewRecursos view;

    public Controller(Model model, viewRecursos view) {
        this.model = model;
        this.view = view;
        view.setController(this);
        view.setModel(model);
    }

    public void refresh() throws Exception {
        model.setCategorias(Service.instance().findAllCategorias());
        model.setRecursos(Service.instance().findAllRecursos());
        clear();
    }

    public void search(CategoriaRecurso categoria, String descripcion) throws Exception {
        String categoriaId = categoria == null || categoria.getID() == null ? "" : categoria.getID();
        model.setRecursos(Service.instance().searchRecursos(categoriaId, descripcion));
    }

    public void clear() {
        model.setCurrent(new Recurso(), false);
    }

    public void save(Recurso r) throws Exception {
        if (model.isEditando()) {
            Service.instance().updateRecurso(r);
        } else {
            Service.instance().createRecurso(r);
        }
        refresh();
    }

    public void delete(String id) throws Exception {
        if (id == null || id.isBlank()) throw new Exception("Seleccione un recurso para borrar");
        Service.instance().deleteRecurso(id);
        refresh();
    }

    public void selectRow(int row) {
        if (row >= 0 && row < model.getRecursos().size()) {
            model.setCurrent(model.getRecursos().get(row), true);
        }
    }

    public void imprimir(String ruta) throws Exception {
        if (model.getRecursos().isEmpty()) throw new Exception("No hay recursos para imprimir");
        PDFReportGenerator.generarReporteRecursos(model.getRecursos(), ruta);
    }
}
