package reservas.presentation.categorias;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Service;
import reservas.presentation.util.PDFReportGenerator;

public class Controller {
    private final Model model;
    private final viewCategorias view;

    public Controller(Model model, viewCategorias view) {
        this.model = model;
        this.view = view;
        view.setModel(model);
        view.setController(this);
    }

    public void refresh() throws Exception {
        model.setCategorias(Service.instance().findAllCategorias());
        clear();
    }

    public void search(String filtro) throws Exception {
        model.setCategorias(Service.instance().searchCategorias(filtro));
    }

    /** Sin id = nueva (el Backend genera CAT-000001...); con id = modificar. */
    public void save(String descripcion) throws Exception {
        if (descripcion == null || descripcion.isBlank()) {
            throw new Exception("La descripción de la categoría no puede estar vacía.");
        }
        CategoriaRecurso actual = model.getCurrent();
        CategoriaRecurso c = new CategoriaRecurso(actual.getID(), descripcion.trim());
        if (c.getID() == null) {
            Service.instance().createCategoria(c);
        } else {
            Service.instance().updateCategoria(c);
        }
        refresh();
    }

    public void delete() throws Exception {
        CategoriaRecurso categoria = model.getCurrent();
        if (categoria == null || categoria.getID() == null) {
            throw new Exception("Debe seleccionar una categoría válida para borrar.");
        }
        Service.instance().deleteCategoria(categoria.getID());
        refresh();
    }

    public void clear() {
        model.setCurrent(new CategoriaRecurso());
    }

    public void selectRow(int row) {
        if (row >= 0 && row < model.getCategorias().size()) {
            model.setCurrent(model.getCategorias().get(row));
        }
    }

    public void imprimir(String ruta) throws Exception {
        if (model.getCategorias().isEmpty()) throw new Exception("No hay categorías para imprimir");
        PDFReportGenerator.generarReporteCategorias(model.getCategorias(), ruta);
    }
}
