package reservas.presentation.cambiarContra;

import reservas.presentation.Highlighter;
import reservas.presentation.util.Iconos;

import javax.swing.*;
import java.awt.Color;

public class viewCambiarContra {
    private JPasswordField actualtxt;
    private JPasswordField nueva1txt;
    private JButton confirmarButton;
    private JPasswordField nueva2txt;
    private JButton cancelarButton;
    private JPanel panelContra;

    private Model model;
    private Controller controller;

    public viewCambiarContra() {
        confirmarButton.setIcon(Iconos.cargarIcono("ok.png", 24, 24));
        cancelarButton.setIcon(Iconos.cargarIcono("cancel.png", 24, 24));

        confirmarButton.addActionListener(e -> controller.cambiarClave());
        cancelarButton.addActionListener(e -> controller.cancelar());

        Highlighter highlighter = new Highlighter(Color.green);
        actualtxt.addMouseListener(highlighter);
        nueva1txt.addMouseListener(highlighter);
        nueva2txt.addMouseListener(highlighter);
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
        return new String(actualtxt.getPassword());
    }

    public String getClaveNueva() {
        return new String(nueva1txt.getPassword());
    }

    public String getClaveNuevaConfirm() {
        return new String(nueva2txt.getPassword());
    }
}
