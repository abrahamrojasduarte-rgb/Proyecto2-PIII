package reservas.presentation.util;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.io.File;

/** Dialogos comunes de las vistas (para no repetir JOptionPane en cada boton). */
public class Mensajes {

    public static void info(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, Exception ex) {
        String msg = ex.getMessage() == null || ex.getMessage().isBlank() ? ex.toString() : ex.getMessage();
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirmar(Component parent, String mensaje, String titulo) {
        return JOptionPane.showConfirmDialog(parent, mensaje, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Pide la ruta de un PDF; devuelve null si el usuario cancela. Agrega ".pdf" si falta. */
    public static String rutaPdf(Component parent, String nombreSugerido) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar reporte PDF");
        chooser.setSelectedFile(new File(nombreSugerido));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return null;
        String ruta = chooser.getSelectedFile().getAbsolutePath();
        return ruta.toLowerCase().endsWith(".pdf") ? ruta : ruta + ".pdf";
    }
}
