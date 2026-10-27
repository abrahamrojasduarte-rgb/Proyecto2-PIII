package reservas.presentation.login;

import reservas.logic.Funcionario;
import reservas.presentation.Highlighter;
import reservas.presentation.util.Iconos;

import javax.swing.*;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class viewLogin implements PropertyChangeListener {
    private JTextField idTXT;
    private JPasswordField txtpass;
    private JButton iniciarSesionButton;
    private JButton cancelarButton;
    private JButton cambiarContrasenaButton;
    private JPanel panelLog;

    private Model model;
    private Controller controller;

    public viewLogin() {
        iniciarSesionButton.setIcon(Iconos.cargarIcono("login.png", 24, 24));
        cancelarButton.setIcon(Iconos.cargarIcono("cancel.png", 24, 24));
        cambiarContrasenaButton.setIcon(Iconos.cargarIcono("clave.png", 24, 24));

        iniciarSesionButton.addActionListener(e -> controller.login());
        txtpass.addActionListener(e -> controller.login());          // Enter en la clave
        cancelarButton.addActionListener(e -> controller.clear());
        cambiarContrasenaButton.addActionListener(e -> controller.cambiarClave());

        Highlighter highlighter = new Highlighter(Color.green);
        idTXT.addMouseListener(highlighter);
        txtpass.addMouseListener(highlighter);
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
        return new String(txtpass.getPassword());
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
