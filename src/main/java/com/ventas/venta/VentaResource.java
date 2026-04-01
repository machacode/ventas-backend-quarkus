package com.ventas.venta;

import com.ventas.common.PageResponse;
import com.ventas.venta.dto.VentaDTO;
import com.ventas.venta.dto.VentaRequest;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/ventas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "VENDEDOR"})
public class VentaResource {

    @Inject VentaService ventaService;
    @Inject JsonWebToken  jwt;

    // ── GET /api/ventas ───────────────────────────
    @GET
    public PageResponse<VentaDTO> listar(
            @QueryParam("page")   @DefaultValue("0")  int page,
            @QueryParam("size")   @DefaultValue("10") int size,
            @QueryParam("estado")                     String estado) {

        return ventaService.listar(page, size, estado);
    }

    // ── GET /api/ventas/{id} ──────────────────────
    @GET
    @Path("/{id}")
    public VentaDTO buscarPorId(@PathParam("id") Long id) {
        return ventaService.buscarPorId(id);
    }

    // ── POST /api/ventas ──────────────────────────
    @POST
    public Response registrar(@Valid VentaRequest request) {
        Long usuarioId = Long.parseLong(jwt.getSubject());
        VentaDTO dto   = ventaService.registrar(request, usuarioId);
        return Response.status(201).entity(dto).build();
    }

    // ── PUT /api/ventas/{id}/anular → solo ADMIN ──
    @PUT
    @Path("/{id}/anular")
    @RolesAllowed("ADMIN")
    public VentaDTO anular(@PathParam("id") Long id) {
        Long usuarioId = Long.parseLong(jwt.getSubject());
        return ventaService.anular(id, usuarioId);
    }
}