package reservas.presentation.alertas;

import reservas.logic.AlertaReserva;
import reservas.presentation.util.Iconos;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.format.DateTimeFormatter;

public class View implements PropertyChangeListener {

    private static final DateTimeFormatter F_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private final JPanel panel;
    private final JTable table;

    private final Icon iconoVer =
            Iconos.cargarIcono("ver.png", 18, 18);

    private final Icon iconoDescartar =
            Iconos.cargarIcono("remove.png", 18, 18);

    private Model model;
    private Controller controller;

    public View() {
        panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(new EmptyBorder(4, 4, 4, 4));
        panel.setPreferredSize(new Dimension(280, 400));

        JLabel titulo = new JLabel("Alertas");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD));
        panel.add(titulo, BorderLayout.NORTH);

        table = new JTable();
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultEditor(Object.class, null);
        table.getTableHeader().setReorderingAllowed(false);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row < 0 || col < 0) return;

                int columna = table.convertColumnIndexToModel(col);

                if (columna == TableModel.VER) {
                    controller.ver(row);
                } else if (columna == TableModel.DESCARTAR) {
                    controller.descartar(row);
                } else if (e.getClickCount() == 2) {
                    controller.ver(row);
                }
            }
        });

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());

                boolean icono = col >= 0 &&
                        table.convertColumnIndexToModel(col)
                                != TableModel.ACTIVIDAD;

                table.setCursor(Cursor.getPredefinedCursor(
                        icono ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR
                ));
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    public JTable getTable() {
        return table;
    }

    public void setModel(Model model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Model.LIST.equals(evt.getPropertyName())) {

            int[] cols = {
                    TableModel.ACTIVIDAD,
                    TableModel.VER,
                    TableModel.DESCARTAR
            };

            table.setModel(new TableModel(
                    cols,
                    model.getAlertas(),
                    iconoVer,
                    iconoDescartar
            ));

            TableColumnModel cm = table.getColumnModel();
            cm.getColumn(0).setPreferredWidth(200);

            for (int i = 1; i <= 2; i++) {
                cm.getColumn(i).setPreferredWidth(38);
                cm.getColumn(i).setMaxWidth(44);
            }

            panel.revalidate();
            panel.repaint();
        }
    }

    public void mostrarDetalle(AlertaReserva alerta) {
        JOptionPane.showMessageDialog(
                panel,
                crearPanelDetalle(alerta),
                alerta.getDescripcion(),
                JOptionPane.PLAIN_MESSAGE
        );
    }

    public boolean confirmarDescarte(AlertaReserva alerta) {
        return JOptionPane.showConfirmDialog(
                panel,
                alerta.getDescripcion(),
                "Descartar?",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    public static JPanel crearPanelDetalle(AlertaReserva alerta) {
        JPanel p = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 12);
        c.anchor = GridBagConstraints.WEST;

        String[][] filas = {
                {"Encargado:", alerta.getEncargado()},
                {"Fecha:", alerta.getFecha() == null
                        ? "" : alerta.getFecha().toString()},
                {"Desde:", alerta.getDesde() == null
                        ? "" : alerta.getDesde().format(F_HORA)},
                {"Hasta:", alerta.getHasta() == null
                        ? "" : alerta.getHasta().format(F_HORA)}
        };

        for (int i = 0; i < filas.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            p.add(new JLabel(filas[i][0]), c);

            c.gridx = 1;
            c.weightx = 1;
            p.add(new JLabel(filas[i][1]), c);
        }

        JList<String> recursos = new JList<>(
                alerta.getRecursos().toArray(new String[0])
        );

        recursos.setEnabled(false);

        JScrollPane scroll = new JScrollPane(recursos);
        scroll.setPreferredSize(new Dimension(180, 90));

        c.gridx = 0;
        c.gridy = filas.length;
        c.weightx = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel("Recursos"), c);

        c.gridx = 1;
        c.fill = GridBagConstraints.BOTH;
        p.add(scroll, c);

        return p;
    }
}