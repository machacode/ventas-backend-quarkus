package com.ventas.producto;

import com.ventas.producto.dto.ProductoDTO;
import com.ventas.producto.dto.ProductoRequest;
import com.ventas.common.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/productos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "VENDEDOR"})
public class ProductoResource {

    @Inject
    ProductoService productoService;

    // ── GET /api/productos ────────────────────────
    @GET
    public PageResponse<ProductoDTO> listar(
            @QueryParam("page")   @DefaultValue("0")  int page,
            @QueryParam("size")   @DefaultValue("10") int size,
            @QueryParam("nombre") String nombre,
            @QueryParam("codigo") String codigo) {

        return productoService.listar(page, size, nombre, codigo);
    }

    // ── GET /api/productos/{id} ───────────────────
    @GET
    @Path("/{id}")
    public ProductoDTO buscarPorId(@PathParam("id") Long id) {
        return productoService.buscarPorId(id);
    }

    // ── POST /api/productos ───────────────────────
    @POST
    @RolesAllowed("ADMIN")
    public Response crear(@Valid ProductoRequest request) {
        ProductoDTO dto = productoService.crear(request);
        return Response.status(201).entity(dto).build();
    }

    // ── PUT /api/productos/{id} ───────────────────
    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public ProductoDTO actualizar(
            @PathParam("id") Long id,
            @Valid ProductoRequest request) {

        return productoService.actualizar(id, request);
    }

    // ── DELETE /api/productos/{id} ────────────────
    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response eliminar(@PathParam("id") Long id) {
        productoService.eliminar(id);
        return Response.noContent().build();
    }
}