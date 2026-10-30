
package reservas.presentation.alertas;

import reservas.logic.AlertaReserva;
import reservas.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    public static final String LIST = "list";

    private final List<AlertaReserva> alertas = new ArrayList<>();

    public List<AlertaReserva> getAlertas() {
        return alertas;
    }

    public void agregar(AlertaReserva alerta) {
        alertas.add(0, alerta);
        firePropertyChange(LIST);
    }

    public AlertaReserva get(int row) {
        return row >= 0 && row < alertas.size()
                ? alertas.get(row)
                : null;
    }

    public void remover(int row) {
        if (row >= 0 && row < alertas.size()) {
            alertas.remove(row);
            firePropertyChange(LIST);
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(LIST);
    }
}