package reservas.presentation.funcionarios;

import reservas.Application;
import reservas.logic.Funcionario;
import reservas.logic.Rol;
import reservas.presentation.Highlighter;
import reservas.presentation.util.Iconos;
import reservas.presentation.util.Mensajes;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class viewFuncionarios implements PropertyChangeListener {
    private JPanel FuncionarioAdmin;
    private JPanel busquedaPanel;
    private JTextField idBuscar;
    private JTextField nombreBuscar;
    private JButton buscarButton;
    private JButton imprimirButton;
    private JPanel Funcionario;
    private JTextField IDregistro;
    private JTextField nombreRegistro;
    private JTextField telefonoRegistro;
    private JButton guardarButton;
    private JButton borrarButton;
    private JButton limpiarButton;
    private JTable funcionarioTable;

    private Controller controller;
    private Model model;

    public viewFuncionarios() {
        buscarButton.setIcon(Iconos.cargarIcono("search.png", 24, 24));
        imprimirButton.setIcon(Iconos.cargarIcono("pdf.png", 24, 24));
        guardarButton.setIcon(Iconos.cargarIcono("save.png", 24, 24));
        borrarButton.setIcon(Iconos.cargarIcono("cancel.png", 24, 24));
        limpiarButton.setIcon(Iconos.cargarIcono("clear.png", 24, 24));

        guardarButton.addActionListener(e -> {
            if (!validar()) return;
            try {
                controller.save(take());
                Mensajes.info(FuncionarioAdmin, "REGISTRO APLICADO");
            } catch (Exception ex) {
                Mensajes.error(FuncionarioAdmin, ex);
            }
        });

        borrarButton.addActionListener(e -> {
            String id = IDregistro.getText().trim();
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(FuncionarioAdmin, "Seleccione o ingrese un ID para borrar", "", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!Mensajes.confirmar(FuncionarioAdmin, "¿Borrar el funcionario " + id + "?", "Borrar")) return;
            try {
                controller.delete(id);
                Mensajes.info(FuncionarioAdmin, "REGISTRO ELIMINADO");
            } catch (Exception ex) {
                Mensajes.error(FuncionarioAdmin, ex);
            }
        });

        buscarButton.addActionListener(e -> {
            try {
                if (controller.search(idBuscar.getText(), nombreBuscar.getText()) == 0) {
                    Mensajes.info(FuncionarioAdmin, "No se encontraron resultados");
                }
            } catch (Exception ex) {
                Mensajes.error(FuncionarioAdmin, ex);
            }
        });

        limpiarButton.addActionListener(e -> {
            limpiarErrores();
            controller.clear();
        });

        imprimirButton.addActionListener(e -> {
            String ruta = Mensajes.rutaPdf(FuncionarioAdmin, "Reporte_Funcionarios.pdf");
            if (ruta == null) return;
            try {
                controller.imprimir(ruta);
                Mensajes.info(FuncionarioAdmin, "PDF generado con éxito en:\n" + ruta);
            } catch (Exception ex) {
                Mensajes.error(FuncionarioAdmin, ex);
            }
        });

        Highlighter highlighter = new Highlighter(Color.green);
        IDregistro.addMouseListener(highlighter);
        nombreRegistro.addMouseListener(highlighter);
        telefonoRegistro.addMouseListener(highlighter);
        idBuscar.addMouseListener(highlighter);
        nombreBuscar.addMouseListener(highlighter);

        funcionarioTable.setDefaultEditor(Object.class, null);
        funcionarioTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                limpiarErrores();
                controller.selectRow(funcionarioTable.getSelectedRow());
            }
        });
    }

    public JPanel getFuncionarioAdmin() {
        return FuncionarioAdmin;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setModel(Model model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Model.CURRENT.equals(evt.getPropertyName())) {
            Funcionario current = model.getCurrent();
            IDregistro.setText(current.getId() == null ? "" : current.getId());
            IDregistro.setEnabled(!model.isEditando());      // el id no se cambia al modificar
            nombreRegistro.setText(current.getNombre() == null ? "" : current.getNombre());
            telefonoRegistro.setText(current.getTelefono() == null ? "" : current.getTelefono());
        } else if (Model.LIST.equals(evt.getPropertyName())) {
            int[] cols = {TableModel.ID, TableModel.NOMBRE, TableModel.NUMTELEFONO, TableModel.ROL};
            funcionarioTable.setModel(new TableModel(cols, model.getFuncionarios()));
        }
        FuncionarioAdmin.revalidate();
        FuncionarioAdmin.repaint();
    }

    private boolean validar() {
        boolean valid = marcar(IDregistro, "Id requerido");
        valid &= marcar(nombreRegistro, "Nombre requerido");
        valid &= marcar(telefonoRegistro, "Número de teléfono requerido");
        return valid;
    }

    private boolean marcar(JTextField campo, String mensaje) {
        boolean ok = !campo.getText().trim().isEmpty();
        campo.setBackground(ok ? null : Application.BACKGROUND_ERROR);
        campo.setToolTipText(ok ? null : mensaje);
        return ok;
    }

    private void limpiarErrores() {
        for (JTextField c : new JTextField[]{IDregistro, nombreRegistro, telefonoRegistro}) {
            c.setBackground(null);
            c.setToolTipText(null);
        }
    }

    public Funcionario take() {
        Funcionario e = new Funcionario();
        e.setId(IDregistro.getText().trim());
        e.setNombre(nombreRegistro.getText().trim());
        e.setTelefono(telefonoRegistro.getText().trim());
        e.setRol(Rol.FUNCIONARIO);
        return e;
    }
}
