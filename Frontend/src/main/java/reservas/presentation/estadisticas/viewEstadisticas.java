package reservas.presentation.estadisticas;

import com.github.lgooddatepicker.components.DatePicker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import reservas.presentation.util.Iconos;
import reservas.presentation.util.Mensajes;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.List;

public class viewEstadisticas implements PropertyChangeListener {

    private Model model;
    private Controller controller;
    private JPanel panelEstadisticas;
    private JPanel recursosPanel;
    private JPanel actividadesPanell;
    private JPanel fechaPanel;
    private DatePicker desdeRecursosPicker;
    private DatePicker hastaRecursosPicker;
    private JButton cargarRecursosButton;
    private JButton imprimirRecursosButton;
    private JPanel graficoRecursos;
    private JScrollPane recursosScroll;
    private JTable recursosTable;
    private DatePicker desdeActividadesPicker;
    private DatePicker hastaActividadesPicker;
    private JButton cargarActividadesButton;
    private JButton imprimirActividadesButton;
    private JPanel graficoActividades;
    private JScrollPane actividadesScroll;
    private JTable actividadesTable;

    public viewEstadisticas() {
        cargarRecursosButton.setIcon(Iconos.cargarIcono("check.png", 24, 24));
        imprimirRecursosButton.setIcon(Iconos.cargarIcono("pdf.png", 24, 24));
        cargarActividadesButton.setIcon(Iconos.cargarIcono("check.png", 24, 24));
        imprimirActividadesButton.setIcon(Iconos.cargarIcono("pdf.png", 24, 24));
        graficoRecursos.setLayout(new BorderLayout());
        graficoActividades.setLayout(new BorderLayout());
        LocalDate primero = LocalDate.now().withDayOfMonth(1);
        LocalDate ultimo = primero.plusMonths(1).minusDays(1);
        desdeRecursosPicker.setDate(primero);
        hastaRecursosPicker.setDate(ultimo);
        desdeActividadesPicker.setDate(primero);
        hastaActividadesPicker.setDate(ultimo);
        cargarRecursosButton.addActionListener(e -> {
            try {
                controller.cargarRecursos(desdeRecursosPicker.getDate(), hastaRecursosPicker.getDate());
            } catch (Exception ex) {
                Mensajes.error(panelEstadisticas, ex);
            }
        });
        cargarActividadesButton.addActionListener(e -> {
            try {
                controller.cargarActividades(desdeActividadesPicker.getDate(),
                        hastaActividadesPicker.getDate());
            } catch (Exception ex) {
                Mensajes.error(panelEstadisticas, ex);
            }
        });
        imprimirRecursosButton.addActionListener(e -> {
            String ruta = Mensajes.rutaPdf(panelEstadisticas, "recursos.pdf");
            if (ruta == null) return;
            try {
                controller.imprimirRecursos(ruta);
                Mensajes.info(panelEstadisticas, "Reporte generado");
            } catch (Exception ex) {
                Mensajes.error(panelEstadisticas, ex);
            }
        });
        imprimirActividadesButton.addActionListener(e -> {
            String ruta = Mensajes.rutaPdf(panelEstadisticas, "actividades.pdf");
            if (ruta == null) return;
            try {
                controller.imprimirActividades(ruta);
                Mensajes.info(panelEstadisticas, "Reporte generado");
            } catch (Exception ex) {
                Mensajes.error(panelEstadisticas, ex);
            }
        });
        recursosTable.setDefaultEditor(Object.class, null);
        actividadesTable.setDefaultEditor(Object.class, null);
    }
    public void setModel(Model model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }
    public void setController(Controller controller) {
        this.controller = controller;
    }
    public JPanel getPanelEstadisticas() {
        return panelEstadisticas;
    }
    private void mostrarGrafico(JPanel contenedor, List<String[]> datos, String titulo, String ejeX,
                                String serie, Color color) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String[] fila : datos) {
            dataset.addValue(Integer.parseInt(fila[1]), serie, fila[0]);
        }
        JFreeChart chart = ChartFactory.createBarChart(titulo, ejeX, "Cantidad", dataset,
                PlotOrientation.VERTICAL, false, true, false);
        CategoryItemRenderer renderer = chart.getCategoryPlot().getRenderer();
        renderer.setSeriesPaint(0, color);
        if (renderer instanceof BarRenderer) {
            ((BarRenderer) renderer).setBarPainter(new StandardBarPainter()); // barras planas
        }
        contenedor.removeAll();
        contenedor.add(new ChartPanel(chart), BorderLayout.CENTER);
        contenedor.revalidate();
        contenedor.repaint();
    }
    private static DefaultTableModel tabla(List<String[]> datos, String c1, String c2) {
        String[][] filas = datos.toArray(new String[0][]);
        return new DefaultTableModel(filas, new String[]{c1, c2});
    }
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case Model.RECURSOS:
                recursosTable.setModel(tabla(model.getRecursos(), "Categoria", "Cantidad"));
                mostrarGrafico(graficoRecursos, model.getRecursos(), "Recursos Usados", "Categoria",
                        "Recursos", Color.BLUE);
                break;
            case Model.ACTIVIDADES:
                actividadesTable.setModel(tabla(model.getActividades(), "Semana", "Cantidad"));
                mostrarGrafico(graficoActividades, model.getActividades(), "Actividades Realizadas",
                        "Semana",
                        "Actividades", Color.RED);
                break;
        }
        panelEstadisticas.revalidate();
        panelEstadisticas.repaint();
    }
}
