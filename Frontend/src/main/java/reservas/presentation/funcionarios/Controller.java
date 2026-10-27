package reservas.presentation.funcionarios;

import reservas.logic.Funcionario;
import reservas.logic.Service;
import reservas.presentation.util.PDFReportGenerator;

import java.util.List;

public class Controller {
    private final Model model;
    private final viewFuncionarios view;

    public Controller(Model model, viewFuncionarios view) {
        this.model = model;
        this.view = view;
        view.setController(this);
        view.setModel(model);
    }

    public void refresh() throws Exception {
        model.setList(Service.instance().findAll());
        clear();
    }

    public void clear() {
        model.setCurrent(new Funcionario(), false);
    }

    // Crea si es nuevo; modifica si se selecciono de la tabla
    public void save(Funcionario f) throws Exception {
        if (model.isEditando()) {
            Service.instance().update(f);
        } else {
            Service.instance().create(f);
        }
        refresh();
    }

    public void delete(String id) throws Exception {
        if (id == null || id.isBlank()) throw new Exception("Seleccione o ingrese un ID para borrar");
        Service.instance().delete(id);
        refresh();
    }

    // Filtra la tabla por id y/o nombre (vacio = todos)
    public int search(String id, String nombre) throws Exception {
        List<Funcionario> resultados = Service.instance().search(id, nombre);
        model.setList(resultados);
        return resultados.size();
    }

    public void selectRow(int row) {
        if (row >= 0 && row < model.getFuncionarios().size()) {
            model.setCurrent(model.getFuncionarios().get(row), true);
        }
    }

    public void imprimir(String ruta) throws Exception {
        if (model.getFuncionarios().isEmpty()) throw new Exception("No hay funcionarios para imprimir");
        PDFReportGenerator.generarReporteFuncionarios(model.getFuncionarios(), ruta);
    }
}
