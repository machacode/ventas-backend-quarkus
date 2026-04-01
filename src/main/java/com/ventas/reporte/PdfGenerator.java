package com.ventas.reporte;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.ventas.kardex.Kardex;
import com.ventas.kardex.dto.StockDTO;
import com.ventas.venta.Venta;
import jakarta.enterprise.context.ApplicationScoped;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class PdfGenerator {

    // Colores corporativos
    private static final Color COLOR_PRIMARIO  = new Color(30, 90, 160);
    private static final Color COLOR_SECUNDARIO = new Color(240, 245, 255);
    private static final Color COLOR_ALERTA    = new Color(220, 53, 69);
    private static final Color COLOR_EXITO     = new Color(40, 167, 69);
    private static final Color COLOR_GRIS      = new Color(108, 117, 125);

    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DATETIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── REPORTE DE VENTAS ─────────────────────────
    public byte[] generarReporteVentas(
            List<Venta> ventas,
            LocalDateTime desde,
            LocalDateTime hasta) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Encabezado
            agregarEncabezado(doc, "REPORTE DE VENTAS",
                    desde, hasta);

            // Tabla de ventas
            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);
            tabla.setWidths(new float[]{1.5f, 2f, 2.5f, 1.5f, 1.5f, 1.5f});

            // Encabezados de columna
            agregarEncabezadoTabla(tabla, new String[]{
                    "Número", "Fecha", "Cliente",
                    "Subtotal", "IGV", "Total"
            });

            // Datos
            BigDecimal totalGeneral = BigDecimal.ZERO;
            boolean filaPar = false;

            for (Venta v : ventas) {
                Color fondo = filaPar
                        ? COLOR_SECUNDARIO : Color.WHITE;

                String cliente = v.cliente != null
                        ? v.cliente.nombre : "Sin cliente";

                agregarCeldaDato(tabla, v.numero, fondo, Element.ALIGN_LEFT);
                agregarCeldaDato(tabla,
                        v.fecha.format(FMT_FECHA), fondo, Element.ALIGN_CENTER);
                agregarCeldaDato(tabla, cliente, fondo, Element.ALIGN_LEFT);
                agregarCeldaDato(tabla,
                        formatoMoneda(v.subtotal), fondo, Element.ALIGN_RIGHT);
                agregarCeldaDato(tabla,
                        formatoMoneda(v.impuesto), fondo, Element.ALIGN_RIGHT);
                agregarCeldaDato(tabla,
                        formatoMoneda(v.total), fondo, Element.ALIGN_RIGHT);

                if ("COMPLETADA".equals(v.estado)) {
                    totalGeneral = totalGeneral.add(v.total);
                }
                filaPar = !filaPar;
            }

            // Fila de totales
            agregarFilaTotales(tabla,
                    "TOTAL PERÍODO", totalGeneral, 6);

            doc.add(tabla);

            // Resumen
            agregarResumen(doc, ventas);

            // Pie de página
            agregarPiePagina(doc);

            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de ventas", e);
        }

        return baos.toByteArray();
    }

    // ── REPORTE DE KARDEX ─────────────────────────
    public byte[] generarReporteKardex(
            List<Kardex> movimientos,
            String productoNombre,
            LocalDateTime desde,
            LocalDateTime hasta) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, baos);
            doc.open();

            agregarEncabezado(doc,
                    "KARDEX DE INVENTARIO — " + productoNombre,
                    desde, hasta);

            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);
            tabla.setWidths(new float[]{2f, 1.5f, 1.5f, 1.5f, 1.5f, 3f});

            agregarEncabezadoTabla(tabla, new String[]{
                    "Fecha", "Tipo", "Cantidad",
                    "Stock Ant.", "Stock Act.", "Motivo"
            });

            boolean filaPar = false;
            for (Kardex k : movimientos) {
                Color fondo = filaPar
                        ? COLOR_SECUNDARIO : Color.WHITE;

                // Color según tipo de movimiento
                Color colorTipo = "IN".equals(k.tipo)
                        ? COLOR_EXITO
                        : "OUT".equals(k.tipo)
                          ? COLOR_ALERTA : COLOR_GRIS;

                agregarCeldaDato(tabla,
                        k.creadoEn.format(FMT_DATETIME),
                        fondo, Element.ALIGN_CENTER);

                // Celda tipo con color
                PdfPCell celdaTipo = new PdfPCell(
                        new Phrase(k.tipo,
                                FontFactory.getFont(
                                        FontFactory.HELVETICA_BOLD, 9, colorTipo))
                );
                celdaTipo.setBackgroundColor(fondo);
                celdaTipo.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaTipo.setPadding(5f);
                tabla.addCell(celdaTipo);

                agregarCeldaDato(tabla,
                        String.valueOf(k.cantidad),
                        fondo, Element.ALIGN_CENTER);
                agregarCeldaDato(tabla,
                        String.valueOf(k.stockAnterior),
                        fondo, Element.ALIGN_CENTER);
                agregarCeldaDato(tabla,
                        String.valueOf(k.stockPosterior),
                        fondo, Element.ALIGN_CENTER);
                agregarCeldaDato(tabla,
                        k.motivo, fondo, Element.ALIGN_LEFT);

                filaPar = !filaPar;
            }

            doc.add(tabla);
            agregarPiePagina(doc);
            doc.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de kardex", e);
        }

        return baos.toByteArray();
    }

    // ── REPORTE DE STOCK ──────────────────────────
    public byte[] generarReporteStock(List<StockDTO> stocks) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // Título sin rango de fechas
            Font fontTitulo = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 16, COLOR_PRIMARIO);
            Font fontSub = FontFactory.getFont(
                    FontFactory.HELVETICA, 10, COLOR_GRIS);

            doc.add(new Paragraph("SISTEMA DE VENTAS", fontTitulo));
            doc.add(new Paragraph(
                    "REPORTE DE STOCK ACTUAL", fontTitulo));
            doc.add(new Paragraph(
                    "Generado: " + LocalDateTime.now().format(FMT_DATETIME),
                    fontSub));
            doc.add(Chunk.NEWLINE);

            PdfPTable tabla = new PdfPTable(6);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(15f);
            tabla.setWidths(new float[]{1.5f, 3f, 1.5f, 1.5f, 1.5f, 1.5f});

            agregarEncabezadoTabla(tabla, new String[]{
                    "Código", "Nombre", "Stock Actual",
                    "Stock Mínimo", "Estado", "Precio"
            });

            boolean filaPar = false;
            for (StockDTO s : stocks) {
                Color fondo = filaPar
                        ? COLOR_SECUNDARIO : Color.WHITE;

                agregarCeldaDato(tabla,
                        s.codigo, fondo, Element.ALIGN_LEFT);
                agregarCeldaDato(tabla,
                        s.nombre, fondo, Element.ALIGN_LEFT);
                agregarCeldaDato(tabla,
                        String.valueOf(s.stockActual),
                        fondo, Element.ALIGN_CENTER);
                agregarCeldaDato(tabla,
                        String.valueOf(s.stockMinimo),
                        fondo, Element.ALIGN_CENTER);

                // Estado con color
                String estadoTxt = s.stockBajo ? "⚠ BAJO" : "✓ OK";
                Color colorEstado = s.stockBajo
                        ? COLOR_ALERTA : COLOR_EXITO;

                PdfPCell celdaEstado = new PdfPCell(
                        new Phrase(estadoTxt,
                                FontFactory.getFont(
                                        FontFactory.HELVETICA_BOLD, 9, colorEstado))
                );
                celdaEstado.setBackgroundColor(fondo);
                celdaEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
                celdaEstado.setPadding(5f);
                tabla.addCell(celdaEstado);

                agregarCeldaDato(tabla,
                        formatoMoneda(s.precio),
                        fondo, Element.ALIGN_RIGHT);

                filaPar = !filaPar;
            }

            doc.add(tabla);
            agregarPiePagina(doc);
            doc.close();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error generando PDF de stock", e);
        }

        return baos.toByteArray();
    }

    // ── HELPERS ───────────────────────────────────

    private void agregarEncabezado(Document doc, String titulo,
                                   LocalDateTime desde, LocalDateTime hasta)
            throws DocumentException {

        Font fontEmpresa = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 18, COLOR_PRIMARIO);
        Font fontTitulo = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
        Font fontPeriodo = FontFactory.getFont(
                FontFactory.HELVETICA, 10, COLOR_GRIS);

        doc.add(new Paragraph("SISTEMA DE VENTAS", fontEmpresa));
        doc.add(new Paragraph(titulo, fontTitulo));

        if (desde != null && hasta != null) {
            doc.add(new Paragraph(
                    "Período: " + desde.format(FMT_FECHA) +
                            " al " + hasta.format(FMT_FECHA),
                    fontPeriodo));
        }

        doc.add(new Paragraph(
                "Generado: " + LocalDateTime.now().format(FMT_DATETIME),
                fontPeriodo));
        doc.add(Chunk.NEWLINE);
    }

    private void agregarEncabezadoTabla(
            PdfPTable tabla, String[] columnas) {

        Font font = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 10, Color.WHITE);

        for (String col : columnas) {
            PdfPCell celda = new PdfPCell(new Phrase(col, font));
            celda.setBackgroundColor(COLOR_PRIMARIO);
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(8f);
            celda.setBorderColor(COLOR_PRIMARIO);
            tabla.addCell(celda);
        }
    }

    private void agregarCeldaDato(PdfPTable tabla,
                                  String valor, Color fondo, int alineacion) {

        Font font = FontFactory.getFont(
                FontFactory.HELVETICA, 9, Color.BLACK);
        PdfPCell celda = new PdfPCell(new Phrase(valor, font));
        celda.setBackgroundColor(fondo);
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5f);
        celda.setBorderColor(new Color(220, 220, 220));
        tabla.addCell(celda);
    }

    private void agregarFilaTotales(PdfPTable tabla,
                                    String label, BigDecimal total, int columnas) {

        Font font = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Color fondoTotal = new Color(255, 243, 205);

        // Celdas vacías hasta la penúltima
        for (int i = 0; i < columnas - 2; i++) {
            PdfPCell celda = new PdfPCell(new Phrase(
                    i == 0 ? label : "", font));
            celda.setBackgroundColor(fondoTotal);
            celda.setPadding(6f);
            tabla.addCell(celda);
        }

        // Total
        PdfPCell celdaTotal = new PdfPCell(
                new Phrase(formatoMoneda(total), font));
        celdaTotal.setBackgroundColor(fondoTotal);
        celdaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaTotal.setPadding(6f);
        tabla.addCell(celdaTotal);
    }

    private void agregarResumen(Document doc, List<Venta> ventas)
            throws DocumentException {

        long completadas = ventas.stream()
                .filter(v -> "COMPLETADA".equals(v.estado)).count();
        long anuladas = ventas.stream()
                .filter(v -> "ANULADA".equals(v.estado)).count();

        Font font = FontFactory.getFont(
                FontFactory.HELVETICA, 10, COLOR_GRIS);

        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
                "Total ventas: " + ventas.size() +
                        "  |  Completadas: " + completadas +
                        "  |  Anuladas: " + anuladas, font));
    }

    private void agregarPiePagina(Document doc)
            throws DocumentException {

        Font font = FontFactory.getFont(
                FontFactory.HELVETICA, 8, COLOR_GRIS);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph(
                "Sistema de Ventas — Documento generado automáticamente",
                font));
    }

    private String formatoMoneda(BigDecimal valor) {
        if (valor == null) return "$0.00";
        return String.format("$%,.2f", valor);
    }
}