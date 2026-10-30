package reservas.presentation.alertas;

import reservas.logic.AlertListener;
import reservas.logic.AlertaReserva;
import reservas.logic.Protocol;

public class Controller implements AlertListener {

    private final Model model;
    private final View view;

    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;

        view.setModel(model);
        view.setController(this);
    }

    @Override
    public void alerta(int tipo, Object payload) {
        if (tipo == Protocol.NOTIFY_RESERVA
                && payload instanceof AlertaReserva) {

            model.agregar((AlertaReserva) payload);
        }
    }

    public void ver(int row) {
        AlertaReserva alerta = model.get(row);

        if (alerta != null) {
            view.mostrarDetalle(alerta);
        }
    }

    public void descartar(int row) {
        AlertaReserva alerta = model.get(row);

        if (alerta != null && view.confirmarDescarte(alerta)) {
            model.remover(row);
        }
    }
}