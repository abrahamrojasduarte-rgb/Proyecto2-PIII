package reservas.presentation.calendarizacion;

import com.github.lgooddatepicker.components.DatePicker;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Recurso;
import reservas.presentation.util.Iconos;
import reservas.presentation.util.Mensajes;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class viewCalendarizacion implements PropertyChangeListener {
    private JTable matrizTable;
    private JPanel panelCalendarizacion;
    private JPanel flitrosPanel;
    private JComboBox categoriaCombo;
    private JButton cargarButton;
    private JButton imprimirButton;
    private DatePicker datePicker;
    private JTable tableMatriz;

    private Model model;
    private Controller controller;
    public viewCalendarizacion() {
        cargarButton.setIcon(Iconos.cargarIcono("ok.png", 24, 24));
        imprimirButton.setIcon(Iconos.cargarIcono("pdf.png", 24, 24));
        datePicker.setDate(LocalDate.now());
        cargarButton.addActionListener(e -> {
            try {
                controller.cargar(datePicker.getDate(), (CategoriaRecurso)
                        categoriaCombo.getSelectedItem());
            } catch (Exception ex) {
                Mensajes.error(panelCalendarizacion, ex);
            }
        });
        imprimirButton.addActionListener(e -> {
            String ruta = Mensajes.rutaPdf(panelCalendarizacion, "calendarizacion.pdf");
            if (ruta == null) return;
            try {
                controller.imprimir(ruta, armarColumnas(), armarFilas());
                Mensajes.info(panelCalendarizacion, "Reporte generado");
            } catch (Exception ex) {
                Mensajes.error(panelCalendarizacion, ex);
            }
        });
        matrizTable.setDefaultEditor(Object.class, null);
    }
    public void setModel(Model model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }
    public void setController(Controller controller) {
        this.controller = controller;
    }
    public JPanel getPanelCalendarizacion() {
        return panelCalendarizacion;
    }
    private String[] armarColumnas() {
        List<Recurso> recursos = model.getRecursos();
        String[] columnas = new String[recursos.size() + 1];
        columnas[0] = "Hora";
        for (int i = 0; i < recursos.size(); i++) {
            columnas[i + 1] = recursos.get(i).getDescripcion();
        }
        return columnas;
    }
    private List<String[]> armarFilas() {
        List<Recurso> recursos = model.getRecursos();
        List<String[]> filas = new ArrayList<>();
        for (int hora = Model.HORA_INICIO; hora <= Model.HORA_FIN; hora++) {
            String[] fila = new String[recursos.size() + 1];
            fila[0] = String.format("%02d:00", hora);
            for (int i = 0; i < recursos.size(); i++) {
                fila[i + 1] = model.buscarTexto(recursos.get(i), hora);
            }
            filas.add(fila);
        }
        return filas;
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case Model.CATEGORIAS:
                Object anterior = categoriaCombo.getSelectedItem();
                DefaultComboBoxModel<CategoriaRecurso> cbModel = new DefaultComboBoxModel<>();
                for (CategoriaRecurso categoria : model.getCategorias()) cbModel.addElement(categoria);
                categoriaCombo.setModel(cbModel);
                if (anterior != null) categoriaCombo.setSelectedItem(anterior);
                break;
            case Model.MATRIZ:
                DefaultTableModel tabla = new DefaultTableModel(armarColumnas(), 0);
                for (String[] fila : armarFilas()) tabla.addRow(fila);
                matrizTable.setModel(tabla);
                break;
        }
        panelCalendarizacion.revalidate();
        panelCalendarizacion.repaint();
    }
}

