package com.ventas.cliente;

import com.ventas.cliente.dto.ClienteDTO;
import com.ventas.cliente.dto.ClienteRequest;
import com.ventas.common.PageResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ClienteService {

    // ── LISTAR con paginación y búsqueda ─────────
    public PageResponse<ClienteDTO> listar(int page, int size, String nombre) {

        // Construir query dinámico
        String query;
        Object[] params;

        if (nombre != null && !nombre.isBlank()) {
            query  = "activo = true AND lower(nombre) like lower(?1)";
            params = new Object[]{"%" + nombre + "%"};
        } else {
            query  = "activo = true";
            params = new Object[]{};
        }

        // Contar total
        long total = Cliente.count(query, params);

        // Obtener página
        List<ClienteDTO> data = Cliente
                .find(query + " ORDER BY nombre ASC", params)
                .page(page, size)
                .list()
                .stream()
                .map(c -> new ClienteDTO((Cliente) c))
                .collect(Collectors.toList());

        return new PageResponse<>(data, total, page, size);
    }

    // ── BUSCAR POR ID ─────────────────────────────
    public ClienteDTO buscarPorId(Long id) {
        Cliente cliente = Cliente.findById(id);

        if (cliente == null || !cliente.activo) {
            throw new NotFoundException("Cliente no encontrado");
        }

        return new ClienteDTO(cliente);
    }

    // ── CREAR ─────────────────────────────────────
    @Transactional
    public ClienteDTO crear(ClienteRequest request) {

        // Verificar documento único
        if (request.documento != null && !request.documento.isBlank()) {
            Cliente existe = Cliente.find(
                    "documento = ?1 AND activo = true",
                    request.documento
            ).firstResult();

            if (existe != null) {
                throw new BadRequestException(
                        "Ya existe un cliente con ese documento"
                );
            }
        }

        Cliente cliente       = new Cliente();
        cliente.nombre        = request.nombre;
        cliente.email         = request.email;
        cliente.telefono      = request.telefono;
        cliente.direccion     = request.direccion;
        cliente.documento     = request.documento;
        cliente.activo        = true;
        cliente.creadoEn      = LocalDateTime.now();
        cliente.actualizadoEn = LocalDateTime.now();

        Cliente.persist(cliente);

        return new ClienteDTO(cliente);
    }

    // ── ACTUALIZAR ────────────────────────────────
    @Transactional
    public ClienteDTO actualizar(Long id, ClienteRequest request) {

        Cliente cliente = Cliente.findById(id);

        if (cliente == null || !cliente.activo) {
            throw new NotFoundException("Cliente no encontrado");
        }

        // Verificar documento único (excluyendo el actual)
        if (request.documento != null && !request.documento.isBlank()) {
            Cliente existe = Cliente.find(
                    "documento = ?1 AND activo = true AND id != ?2",
                    request.documento, id
            ).firstResult();

            if (existe != null) {
                throw new BadRequestException(
                        "Ya existe un cliente con ese documento"
                );
            }
        }

        cliente.nombre        = request.nombre;
        cliente.email         = request.email;
        cliente.telefono      = request.telefono;
        cliente.direccion     = request.direccion;
        cliente.documento     = request.documento;
        cliente.actualizadoEn = LocalDateTime.now();

        return new ClienteDTO(cliente);
    }

    // ── ELIMINAR (lógico) ─────────────────────────
    @Transactional
    public void eliminar(Long id) {

        Cliente cliente = Cliente.findById(id);

        if (cliente == null || !cliente.activo) {
            throw new NotFoundException("Cliente no encontrado");
        }

        // Borrado lógico — nunca borramos físicamente
        cliente.activo        = false;
        cliente.actualizadoEn = LocalDateTime.now();
    }
}