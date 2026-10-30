package reservas.presentation.reserva;

import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.presentation.AbstractTableModel;
import reservas.presentation.util.PDFReportGenerator;

import java.util.List;
import java.util.stream.Collectors;

public class TableModel extends AbstractTableModel<Reserva> {

    public static final int ID = 0;
    public static final int ACTIVIDAD = 1;
    public static final int FECHA = 2;
    public static final int HORARIO = 3;
    public static final int RECURSOS = 4;
    public static final int ESTADO = 5;

    public TableModel(int[] cols, List<Reserva> rows) {
        super(cols, rows);
    }

    @Override
    protected Object getPropetyAt(Reserva reserva, int columnIndex) {
        switch (cols[columnIndex]) {
            case ID:
                return String.format("RES-%06d", reserva.getId());

            case ACTIVIDAD:
                return reserva.getDescripcion();

            case FECHA:
                return reserva.getFecha() == null
                        ? "" : reserva.getFecha().toString();

            case HORARIO:
                return reserva.getDesde() + " - " + reserva.getHasta();

            case RECURSOS:
                return reserva.getRecursos().stream()
                        .map(Recurso::getDescripcion)
                        .collect(Collectors.joining(", "));

            case ESTADO:
                return PDFReportGenerator.estado(reserva);

            default:
                return "";
        }
    }

    @Override
    protected void initColNames() {
        colNames = new String[6];
        colNames[ID] = "Id";
        colNames[ACTIVIDAD] = "Actividad";
        colNames[FECHA] = "Fecha";
        colNames[HORARIO] = "Horario";
        colNames[RECURSOS] = "Recursos";
        colNames[ESTADO] = "Estado";
    }
}