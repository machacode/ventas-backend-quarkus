package com.ventas.reporte;

import com.ventas.kardex.Kardex;
import com.ventas.kardex.dto.StockDTO;
import com.ventas.venta.Venta;
import jakarta.enterprise.context.ApplicationScoped;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class ExcelGenerator {

    private static final DateTimeFormatter FMT_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DATETIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── REPORTE DE VENTAS ─────────────────────────
    public byte[] generarReporteVentas(
            List<Venta> ventas,
            LocalDateTime desde,
            LocalDateTime hasta) {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            XSSFSheet sheet = wb.createSheet("Ventas");

            // Estilos
            CellStyle estTitulo  = estiloTitulo(wb);
            CellStyle estHeader  = estiloHeader(wb);
            CellStyle estDatoPar = estiloDatoPar(wb);
            CellStyle estDatoImpar = estiloDatoImpar(wb);
            CellStyle estMonedaPar = estiloMonedaPar(wb);
            CellStyle estMonedaImpar = estiloMonedaImpar(wb);
            CellStyle estTotal   = estiloTotal(wb);

            int fila = 0;

            // Título
            Row rowTitulo = sheet.createRow(fila++);
            Cell cellTitulo = rowTitulo.createCell(0);
            cellTitulo.setCellValue("REPORTE DE VENTAS — SISTEMA DE VENTAS");
            cellTitulo.setCellStyle(estTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Período
            Row rowPeriodo = sheet.createRow(fila++);
            Cell cellPeriodo = rowPeriodo.createCell(0);
            if (desde != null && hasta != null) {
                cellPeriodo.setCellValue(
                        "Período: " + desde.format(FMT_FECHA) +
                                " al " + hasta.format(FMT_FECHA));
            }
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

            // Generado
            Row rowGen = sheet.createRow(fila++);
            rowGen.createCell(0).setCellValue(
                    "Generado: " + LocalDateTime.now().format(FMT_DATETIME));
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 6));

            fila++; // fila vacía

            // Headers
            Row rowHeader = sheet.createRow(fila++);
            String[] headers = {
                    "Número", "Fecha", "Cliente",
                    "Subtotal", "IGV", "Total", "Estado"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell c = rowHeader.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(estHeader);
            }

            // Datos
            BigDecimal totalGeneral = BigDecimal.ZERO;
            boolean par = false;

            for (Venta v : ventas) {
                Row row = sheet.createRow(fila++);
                CellStyle estDato  = par ? estDatoPar  : estDatoImpar;
                CellStyle estMon   = par ? estMonedaPar : estMonedaImpar;

                row.createCell(0).setCellValue(v.numero);
                row.getCell(0).setCellStyle(estDato);

                row.createCell(1).setCellValue(
                        v.fecha.format(FMT_FECHA));
                row.getCell(1).setCellStyle(estDato);

                row.createCell(2).setCellValue(
                        v.cliente != null ? v.cliente.nombre : "Sin cliente");
                row.getCell(2).setCellStyle(estDato);

                Cell cSub = row.createCell(3);
                cSub.setCellValue(v.subtotal.doubleValue());
                cSub.setCellStyle(estMon);

                Cell cIgv = row.createCell(4);
                cIgv.setCellValue(v.impuesto.doubleValue());
                cIgv.setCellStyle(estMon);

                Cell cTot = row.createCell(5);
                cTot.setCellValue(v.total.doubleValue());
                cTot.setCellStyle(estMon);

                row.createCell(6).setCellValue(v.estado);
                row.getCell(6).setCellStyle(estDato);

                if ("COMPLETADA".equals(v.estado)) {
                    totalGeneral = totalGeneral.add(v.total);
                }
                par = !par;
            }

            // Fila de totales
            Row rowTotal = sheet.createRow(fila++);
            Cell cLabel = rowTotal.createCell(0);
            cLabel.setCellValue("TOTAL PERÍODO");
            cLabel.setCellStyle(estTotal);

            for (int i = 1; i <= 4; i++) {
                rowTotal.createCell(i).setCellStyle(estTotal);
            }

            Cell cTotalVal = rowTotal.createCell(5);
            cTotalVal.setCellValue(totalGeneral.doubleValue());
            cTotalVal.setCellStyle(estiloTotalMoneda(wb));

            rowTotal.createCell(6).setCellStyle(estTotal);

            // Ancho automático de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error generando Excel de ventas", e);
        }
    }

    // ── REPORTE DE KARDEX ─────────────────────────
    public byte[] generarReporteKardex(
            List<Kardex> movimientos,
            String productoNombre,
            LocalDateTime desde,
            LocalDateTime hasta) {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            XSSFSheet sheet = wb.createSheet("Kardex");
            CellStyle estTitulo    = estiloTitulo(wb);
            CellStyle estHeader    = estiloHeader(wb);
            CellStyle estDatoPar   = estiloDatoPar(wb);
            CellStyle estDatoImpar = estiloDatoImpar(wb);

            int fila = 0;

            // Título
            Row rowTitulo = sheet.createRow(fila++);
            Cell cellTitulo = rowTitulo.createCell(0);
            cellTitulo.setCellValue(
                    "KARDEX — " + productoNombre);
            cellTitulo.setCellStyle(estTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            if (desde != null && hasta != null) {
                Row rowPer = sheet.createRow(fila++);
                rowPer.createCell(0).setCellValue(
                        "Período: " + desde.format(FMT_FECHA) +
                                " al " + hasta.format(FMT_FECHA));
                sheet.addMergedRegion(
                        new CellRangeAddress(1, 1, 0, 5));
            }

            fila++;

            // Headers
            Row rowHeader = sheet.createRow(fila++);
            String[] headers = {
                    "Fecha", "Tipo", "Cantidad",
                    "Stock Anterior", "Stock Actual", "Motivo"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell c = rowHeader.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(estHeader);
            }

            // Datos
            boolean par = false;
            for (Kardex k : movimientos) {
                Row row = sheet.createRow(fila++);
                CellStyle est = par ? estDatoPar : estDatoImpar;

                row.createCell(0).setCellValue(
                        k.creadoEn.format(FMT_DATETIME));
                row.getCell(0).setCellStyle(est);

                row.createCell(1).setCellValue(k.tipo);
                row.getCell(1).setCellStyle(est);

                row.createCell(2).setCellValue(k.cantidad);
                row.getCell(2).setCellStyle(est);

                row.createCell(3).setCellValue(k.stockAnterior);
                row.getCell(3).setCellStyle(est);

                row.createCell(4).setCellValue(k.stockPosterior);
                row.getCell(4).setCellStyle(est);

                row.createCell(5).setCellValue(k.motivo);
                row.getCell(5).setCellStyle(est);

                par = !par;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error generando Excel de kardex", e);
        }
    }

    // ── REPORTE DE STOCK ──────────────────────────
    public byte[] generarReporteStock(List<StockDTO> stocks) {

        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            XSSFSheet sheet = wb.createSheet("Stock Actual");
            CellStyle estTitulo    = estiloTitulo(wb);
            CellStyle estHeader    = estiloHeader(wb);
            CellStyle estDatoPar   = estiloDatoPar(wb);
            CellStyle estDatoImpar = estiloDatoImpar(wb);
            CellStyle estAlerta    = estiloAlerta(wb);

            int fila = 0;

            Row rowTitulo = sheet.createRow(fila++);
            Cell cellTitulo = rowTitulo.createCell(0);
            cellTitulo.setCellValue("STOCK ACTUAL — SISTEMA DE VENTAS");
            cellTitulo.setCellStyle(estTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            Row rowGen = sheet.createRow(fila++);
            rowGen.createCell(0).setCellValue(
                    "Generado: " +
                            LocalDateTime.now().format(FMT_DATETIME));
            sheet.addMergedRegion(
                    new CellRangeAddress(1, 1, 0, 5));

            fila++;

            Row rowHeader = sheet.createRow(fila++);
            String[] headers = {
                    "Código", "Nombre", "Stock Actual",
                    "Stock Mínimo", "Estado", "Precio"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell c = rowHeader.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(estHeader);
            }

            boolean par = false;
            for (StockDTO s : stocks) {
                Row row = sheet.createRow(fila++);
                CellStyle est = s.stockBajo
                        ? estAlerta
                        : par ? estDatoPar : estDatoImpar;

                row.createCell(0).setCellValue(s.codigo);
                row.getCell(0).setCellStyle(est);

                row.createCell(1).setCellValue(s.nombre);
                row.getCell(1).setCellStyle(est);

                row.createCell(2).setCellValue(s.stockActual);
                row.getCell(2).setCellStyle(est);

                row.createCell(3).setCellValue(s.stockMinimo);
                row.getCell(3).setCellStyle(est);

                row.createCell(4).setCellValue(
                        s.stockBajo ? "⚠ STOCK BAJO" : "✓ OK");
                row.getCell(4).setCellStyle(est);

                row.createCell(5).setCellValue(
                        s.precio.doubleValue());
                row.getCell(5).setCellStyle(est);

                par = !par;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error generando Excel de stock", e);
        }
    }

    // ── ESTILOS ───────────────────────────────────

    private CellStyle estiloTitulo(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(new XSSFColor(
                new byte[]{(byte)30, (byte)90, (byte)160}, null));
        style.setFont(font);
        return style;
    }

    private CellStyle estiloHeader(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)30, (byte)90, (byte)160}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle estiloDatoPar(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)240, (byte)245, (byte)255}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(
                new XSSFColor(new byte[]{
                        (byte)220, (byte)220, (byte)220}, null));
        return style;
    }

    private CellStyle estiloDatoImpar(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle estiloMonedaPar(XSSFWorkbook wb) {
        XSSFCellStyle style = (XSSFCellStyle) estiloDatoPar(wb);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("$#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle estiloMonedaImpar(XSSFWorkbook wb) {
        XSSFCellStyle style = (XSSFCellStyle) estiloDatoImpar(wb);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("$#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle estiloTotal(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)255, (byte)243, (byte)205}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle estiloTotalMoneda(XSSFWorkbook wb) {
        XSSFCellStyle style = (XSSFCellStyle) estiloTotal(wb);
        DataFormat fmt = wb.createDataFormat();
        style.setDataFormat(fmt.getFormat("$#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle estiloAlerta(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)255, (byte)220, (byte)220}, null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setColor(new XSSFColor(
                new byte[]{(byte)220, (byte)53, (byte)69}, null));
        style.setFont(font);
        return style;
    }
}