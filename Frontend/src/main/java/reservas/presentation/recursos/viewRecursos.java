package reservas.presentation.recursos;

import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.presentation.Highlighter;
import reservas.presentation.util.Iconos;
import reservas.presentation.util.Mensajes;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class viewRecursos implements PropertyChangeListener {

    private JPanel panelRecursos;

    private JTextField idTxt;
    private JTextField descripcionTxt;
    private JComboBox<CategoriaRecurso> categoriaCmb;
    private JComboBox<CategoriaRecurso> buscarCategoriaCmb;
    private JTable recursosTbl;
    private JButton guardarBtn;
    private JButton borrarBtn;
    private JButton limpiarBtn;
    private JButton pdfBtn;
    private JButton buscarBtn;

    private Controller controller;
    private Model model;

    public viewRecursos() {
        buscarBtn.setIcon(Iconos.cargarIcono("search.png", 24, 24));
        pdfBtn.setIcon(Iconos.cargarIcono("pdf.png", 24, 24));
        guardarBtn.setIcon(Iconos.cargarIcono("save.png", 24, 24));
        borrarBtn.setIcon(Iconos.cargarIcono("cancel.png", 24, 24));
        limpiarBtn.setIcon(Iconos.cargarIcono("clear.png", 24, 24));

        recursosTbl.setDefaultEditor(Object.class, null);
        recursosTbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                limpiarErrores();
                controller.selectRow(recursosTbl.getSelectedRow());
            }
        });

        guardarBtn.addActionListener(e -> guardar());
        borrarBtn.addActionListener(e -> borrar());
        limpiarBtn.addActionListener(e -> {
            limpiarErrores();
            controller.clear();
        });
        pdfBtn.addActionListener(e -> generarPDF());
        buscarBtn.addActionListener(e -> {
            try {
                controller.search((CategoriaRecurso) buscarCategoriaCmb.getSelectedItem(), descripcionTxt.getText());
            } catch (Exception ex) {
                Mensajes.error(panelRecursos, ex);
            }
        });

        Highlighter highlighter = new Highlighter(Color.green);
        idTxt.addMouseListener(highlighter);
        descripcionTxt.addMouseListener(highlighter);
    }

    public JPanel getPanelRecursos() {
        return panelRecursos;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setModel(Model model) {
        this.model = model;
        this.model.addPropertyChangeListener(this);
    }

    private void guardar() {
        boolean ok = marcar(idTxt, "Id / número de activo requerido");
        ok &= marcar(descripcionTxt, "Descripción requerida");
        if (categoriaCmb.getSelectedItem() == null) {
            ok = false;
            JOptionPane.showMessageDialog(panelRecursos, "Debe crear y seleccionar una categoría", "", JOptionPane.WARNING_MESSAGE);
        }
        if (!ok) return;
        try {
            Recurso r = new Recurso();
            r.setId(idTxt.getText().trim());
            r.setDescripcion(descripcionTxt.getText().trim());
            r.setCategoria((CategoriaRecurso) categoriaCmb.getSelectedItem());
            controller.save(r);
            Mensajes.info(panelRecursos, "Recurso guardado con éxito");
        } catch (Exception ex) {
            Mensajes.error(panelRecursos, ex);
        }
    }

    private void borrar() {
        String id = idTxt.getText().trim();
        if (!id.isEmpty() && !Mensajes.confirmar(panelRecursos, "¿Borrar el recurso " + id + "?", "Borrar")) return;
        try {
            controller.delete(id);
            Mensajes.info(panelRecursos, "Recurso eliminado");
        } catch (Exception ex) {
            Mensajes.error(panelRecursos, ex);
        }
    }

    private void generarPDF() {
        String ruta = Mensajes.rutaPdf(panelRecursos, "Reporte_Recursos.pdf");
        if (ruta == null) return;
        try {
            controller.imprimir(ruta);
            Mensajes.info(panelRecursos, "PDF generado con éxito en:\n" + ruta);
        } catch (Exception ex) {
            Mensajes.error(panelRecursos, ex);
        }
    }

    private boolean marcar(JTextField campo, String mensaje) {
        boolean ok = !campo.getText().trim().isEmpty();
        campo.setBackground(ok ? null : reservas.Application.BACKGROUND_ERROR);
        campo.setToolTipText(ok ? null : mensaje);
        return ok;
    }

    private void limpiarErrores() {
        idTxt.setBackground(null);
        descripcionTxt.setBackground(null);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case Model.CURRENT:
                Recurso c = model.getCurrent();
                idTxt.setText(c.getId() != null ? c.getId() : "");
                idTxt.setEnabled(!model.isEditando());     // el id no se cambia al modificar
                descripcionTxt.setText(c.getDescripcion() != null ? c.getDescripcion() : "");
                if (c.getCategoria() != null) {
                    categoriaCmb.setSelectedItem(c.getCategoria());   // equals por id
                }
                break;

            case Model.LIST:
                recursosTbl.setModel(new TableModel(
                        new int[]{TableModel.ID, TableModel.CATEGORIA, TableModel.DESCRIPCION}, model.getRecursos()));
                break;

            case Model.CATEGORIAS:
                DefaultComboBoxModel<CategoriaRecurso> cbModel = new DefaultComboBoxModel<>();
                DefaultComboBoxModel<CategoriaRecurso> filterModel = new DefaultComboBoxModel<>();
                filterModel.addElement(new CategoriaRecurso(null, "--- Todas ---"));
                for (CategoriaRecurso cat : model.getCategorias()) {
                    cbModel.addElement(cat);
                    filterModel.addElement(cat);
                }
                categoriaCmb.setModel(cbModel);
                buscarCategoriaCmb.setModel(filterModel);
                break;
        }
        panelRecursos.revalidate();
        panelRecursos.repaint();
    }
}
