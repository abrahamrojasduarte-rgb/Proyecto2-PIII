package reservas;

import reservas.logic.Funcionario;
import reservas.logic.Rol;
import reservas.logic.Service;
import reservas.logic.SocketListener;
import reservas.presentation.util.Iconos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class Application {

    public static final Color BACKGROUND_ERROR = new Color(255, 102, 102);

    private static SocketListener socketListener;

    private interface Recargable {
        void refresh() throws Exception;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(Application::iniciar);
    }

    private static void iniciar() {
        try {
            Service.instance();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    ex.getMessage()
                            + "\n\nVerifique que el Backend este corriendo "
                            + "y la direccion en frontend.properties.",
                    "Sistema de Reservas",
                    JOptionPane.ERROR_MESSAGE
            );

            System.exit(-1);
        }

        doLogin();
    }

    private static void doLogin() {

        reservas.presentation.login.viewLogin view =
                new reservas.presentation.login.viewLogin();

        reservas.presentation.login.Model model =
                new reservas.presentation.login.Model();

        JFrame loginWindow = new JFrame();

        loginWindow.setSize(380, 240);
        loginWindow.setDefaultCloseOperation(
                WindowConstants.DO_NOTHING_ON_CLOSE
        );

        loginWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });

        loginWindow.setTitle("Login - Sistema de Reservas");
        loginWindow.setIconImage(icono());
        loginWindow.setContentPane(view.getPanelLog());
        loginWindow.setLocationRelativeTo(null);

        new reservas.presentation.login.Controller(
                model,
                view,
                loginWindow,
                Application::doRun
        );

        loginWindow.setVisible(true);
    }

    private static void doRun() {

        Funcionario usuario = Sesion.getUsuario();
        boolean admin = Rol.ADMINISTRADOR.equals(usuario.getRol());

        JFrame window = new JFrame(
                "SISTEMA DE RESERVAS - "
                        + usuario.getId()
                        + " (" + usuario.getRol() + ")"
        );

        window.setIconImage(icono());
        window.setDefaultCloseOperation(
                WindowConstants.DO_NOTHING_ON_CLOSE
        );

        JTabbedPane tabbedPane = new JTabbedPane();
        List<Recargable> recargas = new ArrayList<>();

        if (admin) {

            // ===== FUNCIONARIOS =====
            reservas.presentation.funcionarios.Model funcModel =
                    new reservas.presentation.funcionarios.Model();

            reservas.presentation.funcionarios.viewFuncionarios funcView =
                    new reservas.presentation.funcionarios.viewFuncionarios();

            reservas.presentation.funcionarios.Controller funcController =
                    new reservas.presentation.funcionarios.Controller(
                            funcModel, funcView
                    );

            agregar(
                    tabbedPane,
                    recargas,
                    "Funcionarios",
                    "funcionarios.png",
                    funcView.getFuncionarioAdmin(),
                    funcController::refresh
            );

            // ===== CATEGORIAS =====
            reservas.presentation.categorias.Model catModel =
                    new reservas.presentation.categorias.Model();

            reservas.presentation.categorias.viewCategorias catView =
                    new reservas.presentation.categorias.viewCategorias();

            reservas.presentation.categorias.Controller catController =
                    new reservas.presentation.categorias.Controller(
                            catModel, catView
                    );

            agregar(
                    tabbedPane,
                    recargas,
                    "Categorias",
                    "categorias.png",
                    catView.getPanelCategorias(),
                    catController::refresh
            );

            // ===== RECURSOS =====
            reservas.presentation.recursos.Model recModel =
                    new reservas.presentation.recursos.Model();

            reservas.presentation.recursos.viewRecursos recView =
                    new reservas.presentation.recursos.viewRecursos();

            reservas.presentation.recursos.Controller recController =
                    new reservas.presentation.recursos.Controller(
                            recModel, recView
                    );

            agregar(
                    tabbedPane,
                    recargas,
                    "Recursos",
                    "recursos.png",
                    recView.getPanelRecursos(),
                    recController::refresh
            );

        } else {

            // ===== RESERVAS PARA FUNCIONARIOS =====
            reservas.presentation.reserva.Model resModel =
                    new reservas.presentation.reserva.Model();

            reservas.presentation.reserva.viewReserva resView =
                    new reservas.presentation.reserva.viewReserva();

            reservas.presentation.reserva.Controller resController =
                    new reservas.presentation.reserva.Controller(
                            resModel, resView
                    );

            agregar(
                    tabbedPane,
                    recargas,
                    "Reservas",
                    "reservas.png",
                    resView.getPanelReserva(),
                    resController::refresh
            );
        }

        // ===== RECARGAR PESTANAS =====
        tabbedPane.addChangeListener(e -> {
            int i = tabbedPane.getSelectedIndex();

            if (i >= 0) {
                recargar(tabbedPane, recargas.get(i));
            }
        });


        if (!recargas.isEmpty()) {
            recargar(tabbedPane, recargas.get(0));
        }

        // ===== ALERTAS EN TIEMPO REAL =====
        reservas.presentation.alertas.Model alertModel =
                new reservas.presentation.alertas.Model();

        reservas.presentation.alertas.View alertView =
                new reservas.presentation.alertas.View();

        reservas.presentation.alertas.Controller alertController =
                new reservas.presentation.alertas.Controller(
                        alertModel,
                        alertView
                );

        // Abrir canal asincronico despues del login
        try {
            socketListener = new SocketListener(
                    alertController,
                    Service.instance().getSid(),
                    Service.instance().getConfiguracion()
            );

            socketListener.start();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "No se pudo abrir el canal de alertas: "
                            + ex.getMessage(),
                    "Alertas",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        // ===== VENTANA PRINCIPAL =====
        JLabel opciones = new JLabel(
                "Opciones: "
                        + (admin ? "Administrador" : "Funcionario")
                        + " - "
                        + usuario.getNombre()
        );

        opciones.setFont(
                opciones.getFont().deriveFont(Font.BOLD)
        );

        opciones.setBorder(
                new EmptyBorder(0, 2, 4, 0)
        );

        JPanel izquierda = new JPanel(new BorderLayout());

        izquierda.setBorder(
                new EmptyBorder(4, 4, 4, 0)
        );

        izquierda.add(opciones, BorderLayout.NORTH);
        izquierda.add(tabbedPane, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new BorderLayout());

        contenido.add(izquierda, BorderLayout.CENTER);

        // Panel de alertas a la derecha, fuera de las pestanas
        contenido.add(alertView.getPanel(), BorderLayout.EAST);

        window.setContentPane(contenido);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                salir();
            }
        });

        window.setSize(1400, 650);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private static void agregar(
            JTabbedPane tabs,
            List<Recargable> recargas,
            String titulo,
            String icono,
            JComponent panel,
            Recargable recarga
    ) {
        tabs.addTab(
                titulo,
                Iconos.cargarIcono(icono, 24, 24),
                panel
        );

        recargas.add(recarga);
    }

    private static void recargar(
            Component parent,
            Recargable recarga
    ) {
        if (recarga == null) {
            return;
        }

        try {
            recarga.refresh();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    parent,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static Image icono() {
        ImageIcon i = Iconos.cargarIcono(
                "icon.png",
                32,
                32
        );

        return i == null ? null : i.getImage();
    }

    // ===== CERRAR CONEXIONES =====
    private static void salir() {

        // Cerrar canal de alertas
        if (socketListener != null) {
            socketListener.stop();
        }

        // Cerrar conexion principal con Backend
        try {
            Service.instance().stop();
        } catch (Exception ignored) {
        }

        System.exit(0);
    }
}