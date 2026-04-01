package com.ventas.kardex;

import com.ventas.common.PageResponse;
import com.ventas.kardex.dto.KardexDTO;
import com.ventas.kardex.dto.KardexRequest;
import com.ventas.kardex.dto.StockDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDateTime;
import java.util.List;

@Path("/api/kardex")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "VENDEDOR"})
public class KardexResource {

    @Inject KardexService kardexService;
    @Inject JsonWebToken  jwt;

    // ── POST /api/kardex/movimiento ───────────────
    // Solo ADMIN puede registrar movimientos manuales
    @POST
    @Path("/movimiento")
    @RolesAllowed("ADMIN")
    public Response registrarMovimiento(@Valid KardexRequest request)
    {
        Long usuarioId = Long.parseLong(jwt.getSubject());
        KardexDTO dto  = kardexService
                .registrarMovimiento(request, usuarioId);

        return Response.status(201).entity(dto).build();
    }

    // ── GET /api/kardex/producto/{id} ─────────────
    @GET
    @Path("/producto/{id}")
    public PageResponse<KardexDTO> historial(
            @PathParam("id")                          Long productoId,
            @QueryParam("page")   @DefaultValue("0")  int page,
            @QueryParam("size")   @DefaultValue("10") int size,
            @QueryParam("desde")                      String desde,
            @QueryParam("hasta")                      String hasta) {

        // Convertir fechas string → LocalDateTime
        LocalDateTime fechaDesde = desde != null && !desde.isBlank()
                ? LocalDateTime.parse(desde + "T00:00:00") : null;

        LocalDateTime fechaHasta = hasta != null && !hasta.isBlank()
                ? LocalDateTime.parse(hasta + "T23:59:59") : null;

        return kardexService.historialPorProducto(
                productoId, page, size, fechaDesde, fechaHasta
        );
    }

    // ── GET /api/kardex/stock ─────────────────────
    @GET
    @Path("/stock")
    public List<StockDTO> stockActual(
            @QueryParam("soloStockBajo")
            @DefaultValue("false") Boolean soloStockBajo) {

        return kardexService.stockActual(soloStockBajo);
    }

    // ── GET /api/kardex/stock/{productoId} ────────
    @GET
    @Path("/stock/{productoId}")
    public StockDTO stockPorProducto(
            @PathParam("productoId") Long productoId) {

        return kardexService.stockPorProducto(productoId);
    }
}