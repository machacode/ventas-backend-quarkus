package com.ventas.reporte;

import com.ventas.kardex.Kardex;
import com.ventas.kardex.dto.StockDTO;
import com.ventas.producto.Producto;
import com.ventas.venta.Venta;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ReporteService {

    @Inject PdfGenerator   pdfGenerator;
    @Inject ExcelGenerator excelGenerator;

    // ── VENTAS PDF ────────────────────────────────
    public byte[] ventasPdf(
            LocalDateTime desde, LocalDateTime hasta) {

        List<Venta> ventas = obtenerVentas(desde, hasta);
        return pdfGenerator.generarReporteVentas(
                ventas, desde, hasta);
    }

    // ── VENTAS EXCEL ──────────────────────────────
    public byte[] ventasExcel(
            LocalDateTime desde, LocalDateTime hasta) {

        List<Venta> ventas = obtenerVentas(desde, hasta);
        return excelGenerator.generarReporteVentas(
                ventas, desde, hasta);
    }

    // ── KARDEX PDF ────────────────────────────────
    public byte[] kardexPdf(
            Long productoId,
            LocalDateTime desde,
            LocalDateTime hasta) {

        Producto producto = validarProducto(productoId);
        List<Kardex> movs = obtenerKardex(
                productoId, desde, hasta);

        return pdfGenerator.generarReporteKardex(
                movs, producto.nombre, desde, hasta);
    }

    // ── KARDEX EXCEL ──────────────────────────────
    public byte[] kardexExcel(
            Long productoId,
            LocalDateTime desde,
            LocalDateTime hasta) {

        Producto producto = validarProducto(productoId);
        List<Kardex> movs = obtenerKardex(
                productoId, desde, hasta);

        return excelGenerator.generarReporteKardex(
                movs, producto.nombre, desde, hasta);
    }

    // ── STOCK PDF ─────────────────────────────────
    public byte[] stockPdf() {
        List<StockDTO> stocks = obtenerStock();
        return pdfGenerator.generarReporteStock(stocks);
    }

    // ── STOCK EXCEL ───────────────────────────────
    public byte[] stockExcel() {
        List<StockDTO> stocks = obtenerStock();
        return excelGenerator.generarReporteStock(stocks);
    }

    // ── HELPERS PRIVADOS ──────────────────────────

    private List<Venta> obtenerVentas(
            LocalDateTime desde, LocalDateTime hasta) {

        if (desde != null && hasta != null) {
            return Venta.list(
                    "fecha >= ?1 AND fecha <= ?2 ORDER BY fecha DESC",
                    desde, hasta
            );
        }
        return Venta.list("ORDER BY fecha DESC");
    }

    private List<Kardex> obtenerKardex(
            Long productoId,
            LocalDateTime desde,
            LocalDateTime hasta) {

        if (desde != null && hasta != null) {
            return Kardex.list(
                    "producto.id = ?1 AND creadoEn >= ?2 " +
                            "AND creadoEn <= ?3 ORDER BY creadoEn DESC",
                    productoId, desde, hasta
            );
        }
        return Kardex.list(
                "producto.id = ?1 ORDER BY creadoEn DESC",
                productoId
        );
    }

    private Producto validarProducto(Long productoId) {
        Producto p = Producto.findById(productoId);
        if (p == null) {
            throw new NotFoundException("Producto no encontrado");
        }
        return p;
    }

    private List<StockDTO> obtenerStock() {
        return Producto.<Producto>list(
                        "activo = true ORDER BY nombre ASC")
                .stream()
                .map(p -> {
                    Kardex ultimo = Kardex.find(
                            "producto.id = ?1 ORDER BY creadoEn DESC",
                            p.id).firstResult();
                    LocalDateTime ultimoMov = ultimo != null
                            ? ultimo.creadoEn : p.creadoEn;
                    return new StockDTO(p, ultimoMov);
                })
                .collect(Collectors.toList());
    }
}