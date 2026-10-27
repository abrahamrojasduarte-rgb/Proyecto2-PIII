package reservas.presentation;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Highlighter extends MouseAdapter {
    private final Color color;
    private Color original;

    public Highlighter(Color color) {
        this.color = color;
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
        Component source = (Component) evt.getSource();
        original = source.getBackground();
        source.setBackground(color);
    }

    @Override
    public void mouseExited(MouseEvent evt) {
        Component source = (Component) evt.getSource();
        source.setBackground(original);
    }
}
