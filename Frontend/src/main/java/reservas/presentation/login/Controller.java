package reservas.presentation.login;

import reservas.Sesion;
import reservas.logic.Funcionario;
import reservas.logic.Service;

import javax.swing.*;

public class Controller {
    private final Model model;
    private final viewLogin view;
    private final JFrame loginWindow;
    private final Runnable onLoginSuccess;

    public Controller(Model model, viewLogin view, JFrame loginWindow, Runnable onLoginSuccess) {
        this.model = model;
        this.view = view;
        this.loginWindow = loginWindow;
        this.onLoginSuccess = onLoginSuccess;

        view.setController(this);
        view.setModel(model);
    }

    public void clear() {
        model.setCurrent(new Funcionario());
    }

    public void login() {
        String id = view.getID();
        String clave = view.getClave();

        if (id.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(view.getPanelLog(), "Ingrese ID y clave",
                    "Error de acceso", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Funcionario f = new Funcionario();
        f.setId(id);
        f.setClave(clave);

        try {
            Funcionario real = Service.instance().login(f);   // el backend valida
            Sesion.setUsuario(real);
            loginWindow.dispose();
            onLoginSuccess.run();                              // Application.doRun()
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view.getPanelLog(), ex.getMessage(),
                    "Error de acceso", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cambiarClave() {
        String id = view.getID();
        if (id.isEmpty()) {
            JOptionPane.showMessageDialog(view.getPanelLog(), "Ingrese su ID primero",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(loginWindow, "Cambiar Clave", true);
        reservas.presentation.cambiarContra.Model modelCC = new reservas.presentation.cambiarContra.Model();
        reservas.presentation.cambiarContra.viewCambiarContra viewCC = new reservas.presentation.cambiarContra.viewCambiarContra();
        new reservas.presentation.cambiarContra.Controller(modelCC, viewCC, id, dialog);

        dialog.setContentPane(viewCC.getPanel());
        dialog.pack();
        dialog.setLocationRelativeTo(loginWindow);
        dialog.setVisible(true);
    }
}