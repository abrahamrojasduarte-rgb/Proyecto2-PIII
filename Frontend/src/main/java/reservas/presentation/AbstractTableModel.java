package reservas.presentation;

import java.util.List;

public abstract class AbstractTableModel<E> extends javax.swing.table.AbstractTableModel {
    protected List<E> rows;
    protected int[] cols;
    protected String[] colNames;

    public AbstractTableModel(int[] cols, List<E> rows) {
        this.cols = cols;
        this.rows = rows;
        initColNames();
    }

    public int getColumnCount() { return cols.length; }
    public String getColumnName(int col) { return colNames[cols[col]]; }
    public int getRowCount() { return rows.size(); }
    public Object getValueAt(int row, int col) { return getPropetyAt(rows.get(row), col); }
    public E getRowAt(int row) { return rows.get(row); }

    protected abstract Object getPropetyAt(E e, int col);
    protected abstract void initColNames();
}