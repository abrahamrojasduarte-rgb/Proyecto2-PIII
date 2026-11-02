package reservas.presentation.estadisticas;

import reservas.logic.Recurso;
import reservas.logic.Reserva;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Estadisticas {
    private Estadisticas() { }
    public static List<String[]> recursosPorCategoria(List<Reserva> reservas, LocalDate desde, LocalDate
            hasta) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Reserva reserva : reservas) {
            if (fuera(reserva.getFecha(), desde, hasta)) continue;
            for (Recurso recurso : reserva.getRecursos()) {
                if (recurso.getCategoria() == null) continue;
                conteo.merge(recurso.getCategoria().getDescripcion(), 1, Integer::sum);
            }
        }
        List<String[]> datos = new ArrayList<>();
        conteo.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue()) // mas usados primero
                .forEach(e -> datos.add(new String[]{e.getKey(), String.valueOf(e.getValue())}));
        return datos;
    }
    /** Actividades (reservas) por semana; cada fila es {lunes de la semana, cantidad}. */
    public static List<String[]> actividadesPorSemana(List<Reserva> reservas, LocalDate desde, LocalDate
            hasta) {
        List<String[]> datos = new ArrayList<>();
        LocalDate lunes = desde.with(DayOfWeek.MONDAY);
        while (!lunes.isAfter(hasta)) {
            LocalDate domingo = lunes.plusDays(6);
            int cantidad = 0;
            for (Reserva reserva : reservas) {
                LocalDate fecha = reserva.getFecha();
                if (fuera(fecha, desde, hasta)) continue;
                if (!fecha.isBefore(lunes) && !fecha.isAfter(domingo)) cantidad++;
            }
            datos.add(new String[]{lunes.toString(), String.valueOf(cantidad)});
            lunes = lunes.plusDays(7);
        }
        return datos;
    }
    private static boolean fuera(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return fecha == null || fecha.isBefore(desde) || fecha.isAfter(hasta);
    }
}
