package reservas.presentation.recursos;

import reservas.logic.Recurso;
import reservas.presentation.AbstractTableModel;

import java.util.List;

public class TableModel extends AbstractTableModel<Recurso> {
    public static final int ID = 0;
    public static final int CATEGORIA = 1;
    public static final int DESCRIPCION = 2;

    public TableModel(int[] cols, List<Recurso> rows) {
        super(cols, rows);
    }

    @Override
    protected void initColNames() {
        colNames = new String[3];
        colNames[ID] = "ID / Activo";
        colNames[CATEGORIA] = "Categoría";
        colNames[DESCRIPCION] = "Descripción";
    }

    @Override
    protected Object getPropetyAt(Recurso r, int col) {
        switch (cols[col]) {
            case ID: return r.getId();
            case CATEGORIA: return r.getCategoria() != null ? r.getCategoria().getDescripcion() : "";
            case DESCRIPCION: return r.getDescripcion();
            default: return "";
        }
    }
}
