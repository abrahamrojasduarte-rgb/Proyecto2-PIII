package reservas.presentation.reserva;

import com.github.lgooddatepicker.components.DatePicker;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Reserva;
import reservas.presentation.Highlighter;
import reservas.presentation.util.Iconos;
import reservas.presentation.util.Mensajes;

import javax.swing.*;
import java.awt.Color;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class viewReserva implements PropertyChangeListener {

    private Model model;
    private Controller controller;

    private JPanel panelReserva;
    private JPanel nuevaReservaPanel;
    private JPanel categoriasPanel;
    private JPanel misReservasPanel;

    private JScrollPane fraseScroll;
    private JTextArea fraseTxt;
    private JButton extraerButton;
    private JTextField actividadTxt;
    private DatePicker datePicker;
    private JComboBox<String> horaInicioCombo;
    private JComboBox<String> horaFinCombo;
    private JScrollPane categoriasScroll;
    private JList<CategoriaRecurso> categoriasList;

    private JButton reservarButton;
    private JButton cancelarButton;
    private JButton limpiarButton;
    private JButton imprimirButton;
    private JScrollPane reservasScroll;
    private JTable reservasTable;

    public viewReserva() {

        extraerButton.setIcon(
                Iconos.cargarIcono("ai.png", 24, 24));

        limpiarButton.setIcon(
                Iconos.cargarIcono("clear.png", 24, 24));

        cancelarButton.setIcon(
                Iconos.cargarIcono("descartar.png", 24, 24));

        reservarButton.setIcon(
                Iconos.cargarIcono("save.png", 24, 24));

        imprimirButton.setIcon(
                Iconos.cargarIcono("pdf.png", 24, 24));

        extraerButton.addActionListener(e -> {
            panelReserva.setCursor(
                    Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            try {
                controller.extraerConIA(fraseTxt.getText());
            } catch (Exception ex) {
                Mensajes.error(panelReserva, ex);
            } finally {
                panelReserva.setCursor(Cursor.getDefaultCursor());
            }
        });

        reservarButton.addActionListener(e -> {
            try {
                Reserva creada = controller.reservar(
                        actividadTxt.getText(),
                        datePicker.getDate(),
                        hora(horaInicioCombo),
                        hora(horaFinCombo),
                        new ArrayList<>(
                                categoriasList.getSelectedValuesList())
                );

                actividadTxt.setBackground(null);
                fraseTxt.setText("");

                Mensajes.info(
                        panelReserva,
                        "Reserva realizada correctamente.\n"
                                + "Recursos asignados: "
                                + recursos(creada)
                );

            } catch (Exception ex) {
                if (actividadTxt.getText().isBlank()) {
                    actividadTxt.setBackground(
                            reservas.Application.BACKGROUND_ERROR);
                }

                Mensajes.error(panelReserva, ex);
            }
        });

        cancelarButton.addActionListener(e -> {
            int row = reservasTable.getSelectedRow();

            if (row >= 0) {
                row = reservasTable.convertRowIndexToModel(row);
            }

            Reserva reserva = controller.getReserva(row);

            if (reserva != null && !Mensajes.confirmar(
                    panelReserva,
                    "¿Cancelar la reserva \""
                            + reserva.getDescripcion()
                            + "\" del "
                            + reserva.getFecha()
                            + "?",
                    "Cancelar reserva")) {
                return;
            }

            try {
                controller.cancelar(row);
                Mensajes.info(
                        panelReserva,
                        "Reserva cancelada"
                );
            } catch (Exception ex) {
                Mensajes.error(panelReserva, ex);
            }
        });

        limpiarButton.addActionListener(e -> {
            fraseTxt.setText("");
            actividadTxt.setBackground(null);
            controller.clear();
        });

        imprimirButton.addActionListener(e -> {
            String ruta = Mensajes.rutaPdf(
                    panelReserva,
                    "reservas.pdf"
            );

            if (ruta == null) {
                return;
            }

            try {
                controller.imprimir(ruta);

                Mensajes.info(
                        panelReserva,
                        "Reporte generado"
                );
            } catch (Exception ex) {
                Mensajes.error(panelReserva, ex);
            }
        });

        reservasTable.setDefaultEditor(Object.class, null);

        Highlighter highlighter =
                new Highlighter(Color.green);

        actividadTxt.addMouseListener(highlighter);
        fraseTxt.addMouseListener(highlighter);
    }

    private static LocalTime hora(JComboBox<String> combo) {
        Object valor = combo.getSelectedItem();

        return valor == null
                ? null
                : LocalTime.parse(valor.toString());
    }

    private static String recursos(Reserva reserva) {
        List<String> nombres = new ArrayList<>();

        if (reserva != null && reserva.getRecursos() != null) {
            reserva.getRecursos().forEach(
                    recurso -> nombres.add(
                            recurso.getDescripcion())
            );
        }

        return String.join(", ", nombres);
    }

    public void setModel(Model model) {
        this.model = model;
        cargarHoras();
        model.addPropertyChangeListener(this);
    }

    private void cargarHoras() {
        DefaultComboBoxModel<String> inicio =
                new DefaultComboBoxModel<>();

        DefaultComboBoxModel<String> fin =
                new DefaultComboBoxModel<>();

        for (int h = 6; h <= 22; h++) {
            String hora = String.format("%02d:00", h);

            inicio.addElement(hora);
            fin.addElement(hora);
        }

        horaInicioCombo.setModel(inicio);
        horaFinCombo.setModel(fin);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public JPanel getPanelReserva() {
        return panelReserva;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        switch (evt.getPropertyName()) {

            case Model.CATEGORIAS:
                DefaultListModel<CategoriaRecurso> lista =
                        new DefaultListModel<>();

                for (CategoriaRecurso categoria
                        : model.getCategorias()) {
                    lista.addElement(categoria);
                }

                categoriasList.setModel(lista);
                break;

            case Model.CURRENT:
                Reserva current = model.getCurrent();

                actividadTxt.setText(
                        current.getDescripcion() == null
                                ? ""
                                : current.getDescripcion()
                );

                datePicker.setDate(
                        current.getFecha() != null
                                ? current.getFecha()
                                : LocalDate.now()
                );

                seleccionarHora(
                        horaInicioCombo,
                        current.getDesde(),
                        "08:00"
                );

                seleccionarHora(
                        horaFinCombo,
                        current.getHasta(),
                        "09:00"
                );

                marcarCategorias();
                break;

            case Model.LIST:
                int[] cols = {
                        TableModel.ID,
                        TableModel.ACTIVIDAD,
                        TableModel.FECHA,
                        TableModel.HORARIO,
                        TableModel.RECURSOS,
                        TableModel.ESTADO
                };

                reservasTable.setModel(
                        new TableModel(
                                cols,
                                model.getReservas()
                        )
                );
                break;
        }

        panelReserva.revalidate();
        panelReserva.repaint();
    }

    private static void seleccionarHora(
            JComboBox<String> combo,
            LocalTime hora,
            String defecto) {

        String valor = hora == null
                ? defecto
                : hora.toString();

        DefaultComboBoxModel<String> modelo =
                (DefaultComboBoxModel<String>) combo.getModel();

        if (modelo.getIndexOf(valor) < 0) {
            modelo.addElement(valor);
        }

        combo.setSelectedItem(valor);
    }

    private void marcarCategorias() {
        categoriasList.clearSelection();

        List<CategoriaRecurso> seleccionadas =
                model.getSeleccionadas();

        if (seleccionadas.isEmpty()) {
            return;
        }

        List<Integer> indices = new ArrayList<>();

        ListModel<CategoriaRecurso> lista =
                categoriasList.getModel();

        for (int i = 0; i < lista.getSize(); i++) {
            if (seleccionadas.contains(
                    lista.getElementAt(i))) {
                indices.add(i);
            }
        }

        categoriasList.setSelectedIndices(
                indices.stream()
                        .mapToInt(Integer::intValue)
                        .toArray()
        );
    }
}