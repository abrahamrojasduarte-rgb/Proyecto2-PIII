package reservas.presentation.login;

import reservas.logic.Funcionario;
import reservas.presentation.AbstractModel;

import java.beans.PropertyChangeListener;

public class Model extends AbstractModel {
    private Funcionario current = new Funcionario();

    public static final String CURRENT = "current";

    public Funcionario getCurrent() { return current; }

    public void setCurrent(Funcionario current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(CURRENT);
    }
}