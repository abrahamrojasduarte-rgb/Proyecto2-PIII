package reservas.presentation.login;

import reservas.logic.Funcionario;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class viewLogin implements PropertyChangeListener {
    private JTextField idTXT;
    private JTextField txtpass;
    private JButton iniciarSesionButton;
    private JButton cancelarButton;
    private JButton cambiarContrasenaButton;
    private JPanel panelLog;

    private Model model;
    private Controller controller;

    public viewLogin() {
        iniciarSesionButton.addActionListener(e -> controller.login());
        cancelarButton.addActionListener(e -> controller.clear());
        cambiarContrasenaButton.addActionListener(e -> controller.cambiarClave());
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setModel(Model model) {
        this.model = model;
        this.model.addPropertyChangeListener(this);
    }

    public JPanel getPanelLog() {
        return panelLog;
    }

    public String getID() {
        return idTXT.getText().trim();
    }

    public String getClave() {
        return new String(txtpass.getText());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Model.CURRENT.equals(evt.getPropertyName())) {
            Funcionario current = model.getCurrent();
            idTXT.setText(current.getId() != null ? current.getId() : "");
            txtpass.setText("");
        }
    }
}