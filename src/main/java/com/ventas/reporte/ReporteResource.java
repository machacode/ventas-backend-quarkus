package com.ventas.reporte;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;

@Path("/api/reportes")
@RolesAllowed({"ADMIN", "VENDEDOR"})
public class ReporteResource {

    @Inject ReporteService reporteService;

    // ── VENTAS PDF ────────────────────────────────
    @GET
    @Path("/ventas/pdf")
    @Produces("application/pdf")
    public Response ventasPdf(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {

        byte[] pdf = reporteService.ventasPdf(
                parseFecha(desde, false),
                parseFecha(hasta, true)
        );

        return Response.ok(pdf)
                .header("Content-Disposition",
                        "attachment; filename=\"reporte-ventas.pdf\"")
                .header("Content-Type", "application/pdf")
                .build();
    }

    // ── VENTAS EXCEL ──────────────────────────────
    @GET
    @Path("/ventas/excel")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response ventasExcel(
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta) {

        byte[] excel = reporteService.ventasExcel(
                parseFecha(desde, false),
                parseFecha(hasta, true)
        );

        return Response.ok(excel)
                .header("Content-Disposition",
                        "attachment; filename=\"reporte-ventas.xlsx\"")
                .build();
    }

    // ── KARDEX PDF ────────────────────────────────
    @GET
    @Path("/kardex/pdf")
    @Produces("application/pdf")
    public Response kardexPdf(
            @QueryParam("productoId") Long productoId,
            @QueryParam("desde")      String desde,
            @QueryParam("hasta")      String hasta) {

        if (productoId == null) {
            throw new BadRequestException(
                    "El productoId es obligatorio");
        }

        byte[] pdf = reporteService.kardexPdf(
                productoId,
                parseFecha(desde, false),
                parseFecha(hasta, true)
        );

        return Response.ok(pdf)
                .header("Content-Disposition",
                        "attachment; filename=\"reporte-kardex.pdf\"")
                .header("Content-Type", "application/pdf")
                .build();
    }

    // ── KARDEX EXCEL ──────────────────────────────
    @GET
    @Path("/kardex/excel")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response kardexExcel(
            @QueryParam("productoId") Long productoId,
            @QueryParam("desde")      String desde,
            @QueryParam("hasta")      String hasta) {

        if (productoId == null) {
            throw new BadRequestException(
                    "El productoId es obligatorio");
        }

        byte[] excel = reporteService.kardexExcel(
                productoId,
                parseFecha(desde, false),
                parseFecha(hasta, true)
        );

        return Response.ok(excel)
                .header("Content-Disposition",
                        "attachment; filename=\"reporte-kardex.xlsx\"")
                .build();
    }

    // ── STOCK PDF ─────────────────────────────────
    @GET
    @Path("/stock/pdf")
    @Produces("application/pdf")
    public Response stockPdf() {
        byte[] pdf = reporteService.stockPdf();
        return Response.ok(pdf)
                .header("Content-Disposition",
                        "attachment; filename=\"reporte-stock.pdf\"")
                .header("Content-Type", "application/pdf")
                .build();
    }

    // ── STOCK EXCEL ───────────────────────────────
    @GET
    @Path("/stock/excel")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response stockExcel() {
        byte[] excel = reporteService.stockExcel();
        return Response.ok(excel)
                .header("Content-Disposition",
                        "attachment; filename=\"reporte-stock.xlsx\"")
                .build();
    }

    // ── HELPER: parsear fecha ─────────────────────
    private LocalDateTime parseFecha(
            String fecha, boolean esHasta) {

        if (fecha == null || fecha.isBlank()) return null;

        try {
            String hora = esHasta ? "T23:59:59" : "T00:00:00";
            return LocalDateTime.parse(fecha + hora);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Formato de fecha inválido. Use: yyyy-MM-dd");
        }
    }
}