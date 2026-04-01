package com.ventas.producto;

import com.ventas.producto.dto.ProductoDTO;
import com.ventas.producto.dto.ProductoRequest;
import com.ventas.common.PageResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductoService {

    // ── LISTAR ────────────────────────────────────
    public PageResponse<ProductoDTO> listar(
            int page, int size,
            String nombre, String codigo) {

        String query;
        Object[] params;

        if (nombre != null && !nombre.isBlank()) {
            query  = "activo = true AND lower(nombre) like lower(?1)";
            params = new Object[]{"%" + nombre + "%"};
        } else if (codigo != null && !codigo.isBlank()) {
            query  = "activo = true AND lower(codigo) like lower(?1)";
            params = new Object[]{"%" + codigo + "%"};
        } else {
            query  = "activo = true";
            params = new Object[]{};
        }

        long total = Producto.count(query, params);

        List<ProductoDTO> data = Producto
                .find(query + " ORDER BY nombre ASC", params)
                .page(page, size)
                .list()
                .stream()
                .map(p -> new ProductoDTO((Producto) p))
                .collect(Collectors.toList());

        return new PageResponse<>(data, total, page, size);
    }

    // ── BUSCAR POR ID ─────────────────────────────
    public ProductoDTO buscarPorId(Long id) {
        Producto producto = Producto.findById(id);

        if (producto == null || !producto.activo) {
            throw new NotFoundException("Producto no encontrado");
        }

        return new ProductoDTO(producto);
    }

    // ── CREAR ─────────────────────────────────────
    @Transactional
    public ProductoDTO crear(ProductoRequest request) {

        // Verificar código único
        if (Producto.findByCodigo(request.codigo) != null) {
            throw new BadRequestException(
                    "Ya existe un producto con ese codigo"
            );
        }

        Producto producto       = new Producto();
        producto.codigo         = request.codigo;
        producto.nombre         = request.nombre;
        producto.descripcion    = request.descripcion;
        producto.precio         = request.precio;
        producto.stock          = request.stock;
        producto.stockMinimo    = request.stockMinimo;
        producto.unidad         = request.unidad;
        producto.activo         = true;
        producto.creadoEn       = LocalDateTime.now();
        producto.actualizadoEn  = LocalDateTime.now();

        Producto.persist(producto);

        return new ProductoDTO(producto);
    }

    // ── ACTUALIZAR ────────────────────────────────
    @Transactional
    public ProductoDTO actualizar(Long id, ProductoRequest request) {

        Producto producto = Producto.findById(id);

        if (producto == null || !producto.activo) {
            throw new NotFoundException("Producto no encontrado");
        }

        // Verificar código único (excluyendo el actual)
        Producto existe = Producto.find(
                "codigo = ?1 AND id != ?2",
                request.codigo, id
        ).firstResult();

        if (existe != null) {
            throw new BadRequestException(
                    "Ya existe un producto con ese codigo"
            );
        }

        producto.codigo        = request.codigo;
        producto.nombre        = request.nombre;
        producto.descripcion   = request.descripcion;
        producto.precio        = request.precio;
        producto.stockMinimo   = request.stockMinimo;
        producto.unidad        = request.unidad;
        producto.actualizadoEn = LocalDateTime.now();
        // stock NO se actualiza aquí
        // el stock solo cambia via Kardex (Fase 5)

        return new ProductoDTO(producto);
    }

    // ── ELIMINAR (lógico) ─────────────────────────
    @Transactional
    public void eliminar(Long id) {

        Producto producto = Producto.findById(id);

        if (producto == null || !producto.activo) {
            throw new NotFoundException("Producto no encontrado");
        }

        producto.activo        = false;
        producto.actualizadoEn = LocalDateTime.now();
    }
}