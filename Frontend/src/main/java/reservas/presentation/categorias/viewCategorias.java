package reservas.presentation.categorias;

import reservas.logic.CategoriaRecurso;
import reservas.presentation.Highlighter;
import reservas.presentation.util.Iconos;
import reservas.presentation.util.Mensajes;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class viewCategorias implements PropertyChangeListener {
    private Model model;
    private Controller controller;

    private JPanel panelCategorias;
    private JTextField Descripciontxt;     // filtro de busqueda
    private JButton buscarButton;
    private JButton imprimirButton;
    private JPanel busquedaPanel;
    private JPanel categoriaPanel;
    private JTextField idCategoria;
    private JTextField descripcion;
    private JButton guardarButton;
    private JButton borrarButton;
    private JButton limpiarButton;
    private JTable table1;

    public viewCategorias() {
        buscarButton.setIcon(Iconos.cargarIcono("search.png", 24, 24));
        imprimirButton.setIcon(Iconos.cargarIcono("pdf.png", 24, 24));
        guardarButton.setIcon(Iconos.cargarIcono("save.png", 24, 24));
        borrarButton.setIcon(Iconos.cargarIcono("cancel.png", 24, 24));
        limpiarButton.setIcon(Iconos.cargarIcono("clear.png", 24, 24));

        idCategoria.setEditable(false);      // el id lo genera el Backend

        buscarButton.addActionListener(e -> {
            try {
                controller.search(Descripciontxt.getText());
            } catch (Exception ex) {
                Mensajes.error(panelCategorias, ex);
            }
        });

        guardarButton.addActionListener(e -> {
            try {
                controller.save(descripcion.getText());
                descripcion.setBackground(null);
                Mensajes.info(panelCategorias, "Categoría guardada con éxito");
            } catch (Exception ex) {
                descripcion.setBackground(reservas.Application.BACKGROUND_ERROR);
                Mensajes.error(panelCategorias, ex);
            }
        });

        borrarButton.addActionListener(e -> {
            CategoriaRecurso actual = model.getCurrent();
            if (actual.getID() != null &&
                    !Mensajes.confirmar(panelCategorias, "¿Borrar la categoría " + actual.getDescripcion() + "?", "Borrar")) {
                return;
            }
            try {
                controller.delete();
                Mensajes.info(panelCategorias, "Categoría eliminada");
            } catch (Exception ex) {
                Mensajes.error(panelCategorias, ex);
            }
        });

        limpiarButton.addActionListener(e -> {
            descripcion.setBackground(null);
            controller.clear();
        });

        table1.setDefaultEditor(Object.class, null);
        table1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                controller.selectRow(table1.getSelectedRow());
            }
        });

        imprimirButton.addActionListener(e -> {
            String ruta = Mensajes.rutaPdf(panelCategorias, "Reporte_Categorias.pdf");
            if (ruta == null) return;
            try {
                controller.imprimir(ruta);
                Mensajes.info(panelCategorias, "PDF generado con éxito en:\n" + ruta);
            } catch (Exception ex) {
                Mensajes.error(panelCategorias, ex);
            }
        });

        Highlighter highlighter = new Highlighter(Color.green);
        Descripciontxt.addMouseListener(highlighter);
        descripcion.addMouseListener(highlighter);
    }

    public void setModel(Model model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public JPanel getPanelCategorias() {
        return panelCategorias;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case Model.LIST:
                int[] cols = {TableModel.ID, TableModel.DESCRIPCION};
                table1.setModel(new TableModel(cols, model.getCategorias()));
                break;
            case Model.CURRENT:
                CategoriaRecurso current = model.getCurrent();
                idCategoria.setText(current.getID() == null ? "" : current.getID());
                descripcion.setText(current.getDescripcion() == null ? "" : current.getDescripcion());
                break;
        }
        panelCategorias.revalidate();
        panelCategorias.repaint();
    }
}
