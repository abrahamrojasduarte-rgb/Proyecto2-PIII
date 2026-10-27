package reservas.presentation.funcionarios;

import reservas.logic.Funcionario;
import reservas.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    private Funcionario current;
    private boolean editando;          // true si current ya existe en el Backend (se selecciono de la tabla)
    private List<Funcionario> funcionarios;

    public static final String CURRENT = "current";
    public static final String LIST = "list";

    public Model() {
        current = new Funcionario();
        funcionarios = new ArrayList<>();
    }

    public Funcionario getCurrent() {
        return current;
    }

    public boolean isEditando() {
        return editando;
    }

    public void setCurrent(Funcionario current, boolean editando) {
        this.current = current;
        this.editando = editando;
        firePropertyChange(CURRENT);
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setList(List<Funcionario> list) {
        this.funcionarios = list;
        firePropertyChange(LIST);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(CURRENT);
        firePropertyChange(LIST);
    }
}
