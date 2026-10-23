package reservas.presentation.cambiarContra;

import reservas.logic.Funcionario;
import reservas.logic.Service;

import javax.swing.*;

public class Controller {
    private final Model model;
    private final viewCambiarContra view;
    private final String idUsuario;
    private final JDialog dialog;

    public Controller(Model model, viewCambiarContra view, String idUsuario, JDialog dialog) {
        this.model = model;
        this.view = view;
        this.idUsuario = idUsuario;
        this.dialog = dialog;

        view.setController(this);
        view.setModel(model);
    }

    public void cambiarClave() {
        String claveActual = view.getClaveActual();
        String claveNueva = view.getClaveNueva();
        String claveNuevaConfirm = view.getClaveNuevaConfirm();

        if (claveActual.isEmpty() || claveNueva.isEmpty() || claveNuevaConfirm.isEmpty()) {
            JOptionPane.showMessageDialog(view.getPanel(), "Complete todos los campos",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!claveNueva.equals(claveNuevaConfirm)) {
            JOptionPane.showMessageDialog(view.getPanel(), "Las claves nuevas no coinciden",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Funcionario f = new Funcionario();
            f.setId(idUsuario);
            f.setClave(claveActual);                      // el backend la verifica
            Service.instance().cambiarClave(f, claveNueva);

            JOptionPane.showMessageDialog(view.getPanel(), "Clave actualizada correctamente",
                    "", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanel(), ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cancelar() {
        dialog.dispose();
    }
}