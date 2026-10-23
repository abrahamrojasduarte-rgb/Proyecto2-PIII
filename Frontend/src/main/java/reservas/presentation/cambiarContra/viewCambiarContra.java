package reservas.presentation.cambiarContra;

import javax.swing.*;

public class viewCambiarContra {
    private JTextField actualtxt;
    private JTextField nueva1txt;
    private JButton confirmarButton;
    private JTextField nueva2txt;
    private JButton cancelarButton;
    private JPanel panelContra;

    private Model model;
    private Controller controller;

    public viewCambiarContra() {
        confirmarButton.addActionListener(e -> controller.cambiarClave());
        cancelarButton.addActionListener(e -> controller.cancelar());
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public JPanel getPanel() {
        return panelContra;
    }

    public String getClaveActual() {
        return new String(actualtxt.getText());
    }

    public String getClaveNueva() {
        return new String(nueva1txt.getText());
    }

    public String getClaveNuevaConfirm() {
        return new String(nueva2txt.getText());
    }
}

