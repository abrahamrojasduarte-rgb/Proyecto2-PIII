package reservas.presentation.reserva;

import reservas.Sesion;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Reserva;
import reservas.logic.ReservaExtraccion;
import reservas.logic.Service;
import reservas.presentation.util.PDFReportGenerator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class Controller {

    private final Model model;
    private final viewReserva view;

    public Controller(Model model, viewReserva view) {
        this.model = model;
        this.view = view;
        view.setModel(model);
        view.setController(this);
    }

    public void refresh() throws Exception {
        model.setCategorias(Service.instance().findAllCategorias());
        cargarMisReservas();
        clear();
    }

    private void cargarMisReservas() throws Exception {
        model.setReservas(
                Service.instance().findReservasByFuncionario(
                        Sesion.getUsuario().getId()
                )
        );
    }

    public void clear() {
        model.setSeleccionadas(new ArrayList<>());
        model.setCurrent(new Reserva());
    }

    public Reserva reservar(String actividad, LocalDate fecha,
                            LocalTime inicio, LocalTime fin,
                            List<CategoriaRecurso> categorias) throws Exception {

        if (actividad == null || actividad.isBlank())
            throw new Exception("Debe indicar la actividad");

        if (fecha == null)
            throw new Exception("Debe indicar la fecha");

        if (inicio == null || fin == null)
            throw new Exception("Debe indicar el horario");

        if (!fin.isAfter(inicio))
            throw new Exception("La hora de fin debe ser mayor a la hora de inicio");

        if (fecha.isBefore(LocalDate.now()))
            throw new Exception("No se pueden hacer reservas en fechas pasadas");

        if (categorias == null || categorias.isEmpty())
            throw new Exception("Debe seleccionar al menos una categoria");

        Reserva r = new Reserva(actividad.trim(), fecha, inicio, fin);
        r.setFuncionario(Sesion.getUsuario());

        Reserva creada = Service.instance().createReserva(r, categorias);

        cargarMisReservas();
        clear();

        return creada;
    }

    public void cancelar(int row) throws Exception {
        if (row < 0 || row >= model.getReservas().size()) {
            throw new Exception("Debe seleccionar una reserva");
        }

        Reserva reserva = model.getReservas().get(row);

        if (reserva.getFecha().isBefore(LocalDate.now())) {
            throw new Exception("Solo se pueden cancelar reservas futuras");
        }

        Service.instance().deleteReserva(reserva.getId());
        cargarMisReservas();
        clear();
    }

    public Reserva getReserva(int row) {
        return row >= 0 && row < model.getReservas().size()
                ? model.getReservas().get(row)
                : null;
    }

    public void extraerConIA(String frase) throws Exception {
        if (frase == null || frase.isBlank()) {
            throw new Exception("Escriba una frase, por ejemplo: "
                    + "\"Reunión de equipo el próximo lunes de 9am a 11am, "
                    + "necesito una sala y un proyector\"");
        }

        ReservaExtraccion datos = Service.instance().extraerReserva(frase);

        Reserva reserva = new Reserva();
        reserva.setDescripcion(datos.getActividad());

        try {
            if (datos.getFecha() != null)
                reserva.setFecha(LocalDate.parse(datos.getFecha()));

            if (datos.getHoraInicio() != null)
                reserva.setDesde(LocalTime.parse(datos.getHoraInicio()));

            if (datos.getHoraFinal() != null)
                reserva.setHasta(LocalTime.parse(datos.getHoraFinal()));

        } catch (DateTimeParseException ex) {
            throw new Exception(
                    "La IA devolvió una fecha u hora con formato inválido"
            );
        }

        List<CategoriaRecurso> encontradas = new ArrayList<>();

        if (datos.getCategoriasRecurso() != null) {
            for (String nombre : datos.getCategoriasRecurso()) {
                for (CategoriaRecurso categoria : model.getCategorias()) {
                    if (nombre != null
                            && categoria.getDescripcion()
                            .equalsIgnoreCase(nombre.trim())) {
                        encontradas.add(categoria);
                        break;
                    }
                }
            }
        }

        model.setSeleccionadas(encontradas);
        model.setCurrent(reserva);
    }

    public void imprimir(String ruta) throws Exception {
        if (model.getReservas().isEmpty()) {
            throw new Exception("No hay reservas para imprimir");
        }

        PDFReportGenerator.generarReporteReservas(
                model.getReservas(), ruta
        );
    }
}