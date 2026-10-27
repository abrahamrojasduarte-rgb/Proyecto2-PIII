package reservas.presentation.util;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

public class Iconos {

    public static ImageIcon cargarIcono(String nombre, int ancho, int alto) {
        URL url = Iconos.class.getResource("/icons/" + nombre);

        if (url == null) {
            System.err.println("No se encontró el icono: " + nombre);
            return null;
        }

        ImageIcon icon = new ImageIcon(url);
        Image imagen = icon.getImage().getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        return new ImageIcon(imagen);
    }
}
