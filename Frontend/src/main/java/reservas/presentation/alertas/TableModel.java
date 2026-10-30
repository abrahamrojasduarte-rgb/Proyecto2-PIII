package reservas.presentation.alertas;

import reservas.logic.AlertaReserva;
import reservas.presentation.AbstractTableModel;

import javax.swing.Icon;
import java.util.List;

public class TableModel extends AbstractTableModel<AlertaReserva> {

    public static final int ACTIVIDAD = 0;
    public static final int VER = 1;
    public static final int DESCARTAR = 2;

    private final Icon iconoVer;
    private final Icon iconoDescartar;

    public TableModel(int[] cols, List<AlertaReserva> rows,
                      Icon iconoVer, Icon iconoDescartar) {
        super(cols, rows);
        this.iconoVer = iconoVer;
        this.iconoDescartar = iconoDescartar;
    }

    @Override
    protected void initColNames() {
        colNames = new String[3];
        colNames[ACTIVIDAD] = "Actividad";
        colNames[VER] = "Ver";
        colNames[DESCARTAR] = "Desc.";
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return cols[col] == ACTIVIDAD ? String.class : Icon.class;
    }

    @Override
    protected Object getPropetyAt(AlertaReserva a, int col) {
        switch (cols[col]) {
            case ACTIVIDAD:
                return a.toString();
            case VER:
                return iconoVer;
            case DESCARTAR:
                return iconoDescartar;
            default:
                return "";
        }
    }
}