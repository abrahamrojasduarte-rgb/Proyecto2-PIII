package reservas.presentation.util;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import reservas.logic.CategoriaRecurso;
import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PDFReportGenerator {

    private static final DateTimeFormatter F_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter F_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static Paragraph titulo(String texto) {
        return new Paragraph(texto).setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER);
    }

    public static void generarReporteFuncionarios(List<Funcionario> lista, String destPath) throws Exception {
        Document document = new Document(new PdfDocument(new PdfWriter(destPath)));
        document.add(titulo("Reporte de Funcionarios"));

        Table table = new Table(new float[]{100f, 200f, 100f});
        table.addHeaderCell("ID / Cédula");
        table.addHeaderCell("Nombre");
        table.addHeaderCell("Teléfono");
        for (Funcionario f : lista) {
            table.addCell(nn(f.getId()));
            table.addCell(nn(f.getNombre()));
            table.addCell(nn(f.getTelefono()));
        }
        document.add(table);
        document.close();
    }

    public static void generarReporteCategorias(List<CategoriaRecurso> lista, String destPath) throws Exception {
        Document document = new Document(new PdfDocument(new PdfWriter(destPath)));
        document.add(titulo("Reporte de Categorías"));

        Table table = new Table(new float[]{100f, 300f});
        table.addHeaderCell("ID");
        table.addHeaderCell("Descripción");
        for (CategoriaRecurso c : lista) {
            table.addCell(nn(c.getID()));
            table.addCell(nn(c.getDescripcion()));
        }
        document.add(table);
        document.close();
    }

    public static void generarReporteRecursos(List<Recurso> lista, String destPath) throws Exception {
        Document document = new Document(new PdfDocument(new PdfWriter(destPath)));
        document.add(titulo("Reporte de Recursos"));

        Table table = new Table(new float[]{100f, 150f, 200f});
        table.addHeaderCell("ID / Activo");
        table.addHeaderCell("Categoría");
        table.addHeaderCell("Descripción");
        for (Recurso r : lista) {
            table.addCell(nn(r.getId()));
            table.addCell(r.getCategoria() != null ? nn(r.getCategoria().getDescripcion()) : "N/A");
            table.addCell(nn(r.getDescripcion()));
        }
        document.add(table);
        document.close();
    }

    public static void generarReporteReservas(List<Reserva> lista, String destPath) throws Exception {
        Document document = new Document(new PdfDocument(new PdfWriter(destPath)));
        document.add(titulo("Listado de Reservas"));

        Table table = new Table(new float[]{80f, 150f, 80f, 90f, 130f, 80f});
        table.addHeaderCell("Id");
        table.addHeaderCell("Actividad");
        table.addHeaderCell("Fecha");
        table.addHeaderCell("Horario");
        table.addHeaderCell("Recursos");
        table.addHeaderCell("Estado");

        for (Reserva r : lista) {
            table.addCell(String.format("RES-%06d", r.getId()));
            table.addCell(nn(r.getDescripcion()));
            table.addCell(r.getFecha() != null ? r.getFecha().format(F_FECHA) : "");
            String horario = "";
            if (r.getDesde() != null && r.getHasta() != null) {
                horario = r.getDesde().format(F_HORA) + " - " + r.getHasta().format(F_HORA);
            }
            table.addCell(horario);
            table.addCell(r.getRecursos() == null ? "" :
                    r.getRecursos().stream().map(Recurso::getDescripcion).collect(Collectors.joining(", ")));
            table.addCell(estado(r));
        }
        document.add(table);
        document.close();
    }

    /** ACTIVA mientras no haya terminado; FINALIZADA despues. */
    public static String estado(Reserva r) {
        if (r.getFecha() != null && r.getHasta() != null
                && LocalDateTime.of(r.getFecha(), r.getHasta()).isBefore(LocalDateTime.now())) {
            return "FINALIZADA";
        }
        return "ACTIVA";
    }

    public static void generarReporteMatriz(String titulo, String[] columnas, List<String[]> filas, String destPath) throws Exception {
        PdfDocument pdf = new PdfDocument(new PdfWriter(destPath));
        pdf.setDefaultPageSize(PageSize.A4.rotate());
        Document document = new Document(pdf);
        document.add(new Paragraph(titulo).setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));

        Table table = new Table(columnas.length);
        for (String columna : columnas) table.addHeaderCell(columna);
        for (String[] fila : filas) {
            for (String celda : fila) table.addCell(nn(celda));
        }
        document.add(table);
        document.close();
    }

    private static String nn(String s) { return s == null ? "" : s; }
}
