package com.ventas.cliente;

import com.ventas.cliente.dto.ClienteDTO;
import com.ventas.cliente.dto.ClienteRequest;
import com.ventas.common.PageResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN", "VENDEDOR"})  // todos los endpoints requieren login
public class ClienteResource {

    @Inject ClienteService clienteService;

    // ── GET /api/clientes ─────────────────────────
    @GET
    public PageResponse<ClienteDTO> listar(
            @QueryParam("page")   @DefaultValue("0")  int page,
            @QueryParam("size")   @DefaultValue("10") int size,
            @QueryParam("nombre") String nombre) {

        return clienteService.listar(page, size, nombre);
    }

    // ── GET /api/clientes/{id} ────────────────────
    @GET
    @Path("/{id}")
    public ClienteDTO buscarPorId(@PathParam("id") Long id) {
        return clienteService.buscarPorId(id);
    }

    // ── POST /api/clientes ────────────────────────
    @POST
    public Response crear(@Valid ClienteRequest request) {
        ClienteDTO dto = clienteService.crear(request);
        return Response.status(201).entity(dto).build();
    }

    // ── PUT /api/clientes/{id} ────────────────────
    @PUT
    @Path("/{id}")
    public ClienteDTO actualizar(
            @PathParam("id") Long id,
            @Valid ClienteRequest request) {

        return clienteService.actualizar(id, request);
    }

    // ── DELETE /api/clientes/{id} → solo ADMIN ────
    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response eliminar(@PathParam("id") Long id) {
        clienteService.eliminar(id);
        return Response.noContent().build();  // 204
    }
}