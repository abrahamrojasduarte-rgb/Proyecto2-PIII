package reservas.presentation.actividades;

import com.github.lgooddatepicker.components.DatePicker;
import reservas.presentation.util.Iconos;
import reservas.presentation.util.Mensajes;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class viewActividades implements PropertyChangeListener {
    private JPanel semanaPanel;
    private JTable matrizTable;
    private JButton cargarButton;
    private JButton imprimirButton;
    private JPanel panelActividades;
    private DatePicker datePicker;

    private Model model;
    private Controller controller;
    private final DateTimeFormatter formatoColumna = DateTimeFormatter.ofPattern("EEE dd/MM");
    public viewActividades() {
        cargarButton.setIcon(Iconos.cargarIcono("ok.png", 24, 24));
        imprimirButton.setIcon(Iconos.cargarIcono("pdf.png", 24, 24));
        datePicker.setDate(LocalDate.now());
        cargarButton.addActionListener(e -> {
            try {
                controller.cargar(datePicker.getDate());
            } catch (Exception ex) {
                Mensajes.error(panelActividades, ex);
            }
        });
        imprimirButton.addActionListener(e -> {
            String ruta = Mensajes.rutaPdf(panelActividades, "actividades.pdf");
            if (ruta == null) return;try {
                controller.imprimir(ruta, armarColumnas(), armarFilas());
                Mensajes.info(panelActividades, "Reporte generado");
            } catch (Exception ex) {
                Mensajes.error(panelActividades, ex);
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
    public JPanel getPanelActividades() {
        return panelActividades;
    }
    private String[] armarColumnas() {
        String[] columnas = new String[Model.DIAS + 1];
        columnas[0] = "Hora";
        for (int i = 0; i < Model.DIAS; i++) {
            columnas[i + 1] = model.getDia(i).format(formatoColumna);
        }
        return columnas;
    }
    private List<String[]> armarFilas() {
        List<String[]> filas = new ArrayList<>();
        for (int hora = Model.HORA_INICIO; hora <= Model.HORA_FIN; hora++) {
            String[] fila = new String[Model.DIAS + 1];
            fila[0] = String.format("%02d:00", hora);
            for (int i = 0; i < Model.DIAS; i++) {
                fila[i + 1] = model.buscarTexto(model.getDia(i), hora);
            }
            filas.add(fila);
        }
        return filas;
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Model.MATRIZ.equals(evt.getPropertyName())) {
            DefaultTableModel tabla = new DefaultTableModel(armarColumnas(), 0);
            for (String[] fila : armarFilas()) tabla.addRow(fila);
            matrizTable.setModel(tabla);
        }
        panelActividades.revalidate();
        panelActividades.repaint();
    }
}
