package reservas.presentation.funcionarios;

import reservas.logic.Funcionario;
import reservas.presentation.AbstractTableModel;

import java.util.List;

public class TableModel extends AbstractTableModel<Funcionario> {

    public static final int ID = 0;
    public static final int NOMBRE = 1;
    public static final int NUMTELEFONO = 2;
    public static final int ROL = 3;

    public TableModel(int[] cols, List<Funcionario> rows) {
        super(cols, rows);
    }

    @Override
    protected void initColNames() {
        colNames = new String[4];
        colNames[ID] = "Id";
        colNames[NOMBRE] = "Nombre";
        colNames[NUMTELEFONO] = "Numero de Telefono";
        colNames[ROL] = "Rol";
    }

    @Override
    protected Object getPropetyAt(Funcionario f, int col) {
        switch (cols[col]) {
            case ID: return f.getId();
            case NOMBRE: return f.getNombre();
            case NUMTELEFONO: return f.getTelefono();
            case ROL: return f.getRol();
            default: return "";
        }
    }
}
