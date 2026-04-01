package com.ventas.kardex;

import com.ventas.common.PageResponse;
import com.ventas.kardex.dto.KardexDTO;
import com.ventas.kardex.dto.KardexRequest;
import com.ventas.kardex.dto.StockDTO;
import com.ventas.producto.Producto;
import com.ventas.usuario.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class KardexService
{

    // ── REGISTRAR MOVIMIENTO MANUAL ───────────────
    @Transactional
    public KardexDTO registrarMovimiento(
            KardexRequest request, Long usuarioId) {

        // 1. Buscar producto
        Producto producto = Producto.findById(request.productoId);
        if (producto == null || !producto.activo) {
            throw new NotFoundException("Producto no encontrado");
        }

        // 2. Buscar usuario
        Usuario usuario = Usuario.findById(usuarioId);
        if (usuario == null) {
            throw new NotFoundException("Usuario no encontrado");
        }

        int stockAnterior  = producto.stock;
        int stockPosterior;

        // 3. Aplicar movimiento según tipo
        switch (request.tipo) {

            case "IN":
                stockPosterior  = stockAnterior + request.cantidad;
                producto.stock  = stockPosterior;
                break;

            case "OUT":
                if (producto.stock < request.cantidad) {
                    throw new BadRequestException(
                            "Stock insuficiente. Stock actual: " +
                                    producto.stock +
                                    ", solicitado: " + request.cantidad
                    );
                }
                stockPosterior = stockAnterior - request.cantidad;
                producto.stock = stockPosterior;
                break;

            case "ADJUST":
                // ADJUST: cantidad ES el nuevo stock
                stockPosterior = request.cantidad;
                producto.stock = stockPosterior;
                break;

            default:
                throw new BadRequestException(
                        "Tipo invalido: " + request.tipo
                );
        }

        producto.actualizadoEn = LocalDateTime.now();

        // 4. Registrar en Kardex
        Kardex kardex           = new Kardex();
        kardex.producto         = producto;
        kardex.tipo             = request.tipo;
        kardex.cantidad         = request.cantidad;
        kardex.stockAnterior    = stockAnterior;
        kardex.stockPosterior   = stockPosterior;
        kardex.motivo           = request.motivo;
        kardex.usuario          = usuario;
        kardex.creadoEn         = LocalDateTime.now();
        Kardex.persist(kardex);

        return new KardexDTO(kardex);
    }

    // ── HISTORIAL POR PRODUCTO ────────────────────
    public PageResponse<KardexDTO> historialPorProducto(
            Long productoId,
            int page, int size,
            LocalDateTime desde,
            LocalDateTime hasta) {

        // Verificar que el producto existe
        Producto producto = Producto.findById(productoId);
        if (producto == null) {
            throw new NotFoundException("Producto no encontrado");
        }

        // Construir query con filtros de fecha
        String query;
        Object[] params;

        if (desde != null && hasta != null) {
            query  = "producto.id = ?1 AND creadoEn >= ?2 AND creadoEn <= ?3 ORDER BY creadoEn DESC";
            params = new Object[]{productoId, desde, hasta};
        } else if (desde != null) {
            query  = "producto.id = ?1 AND creadoEn >= ?2 ORDER BY creadoEn DESC";
            params = new Object[]{productoId, desde};
        } else if (hasta != null) {
            query  = "producto.id = ?1 AND creadoEn <= ?2 ORDER BY creadoEn DESC";
            params = new Object[]{productoId, hasta};
        } else {
            query  = "producto.id = ?1 ORDER BY creadoEn DESC";
            params = new Object[]{productoId};
        }

        long total = Kardex.count(
                query.replace(" ORDER BY creadoEn DESC", ""), params
        );

        List<KardexDTO> data = Kardex
                .find(query, params)
                .page(page, size)
                .list()
                .stream()
                .map(k -> new KardexDTO((Kardex) k))
                .collect(Collectors.toList());

        return new PageResponse<>(data, total, page, size);
    }

    // ── STOCK ACTUAL DE TODOS LOS PRODUCTOS ───────
    public List<StockDTO> stockActual(Boolean soloStockBajo) {

        List<Producto> productos;

        if (Boolean.TRUE.equals(soloStockBajo)) {
            // Solo productos con stock bajo
            productos = Producto.list(
                    "activo = true AND stock <= stockMinimo ORDER BY nombre ASC"
            );
        } else {
            productos = Producto.list(
                    "activo = true ORDER BY nombre ASC"
            );
        }

        return productos.stream()
                .map(p -> {
                    // Buscar último movimiento de este producto
                    Kardex ultimo = Kardex
                            .find("producto.id = ?1 ORDER BY creadoEn DESC", p.id)
                            .firstResult();

                    LocalDateTime ultimoMov = ultimo != null
                            ? ultimo.creadoEn : p.creadoEn;

                    return new StockDTO(p, ultimoMov);
                })
                .collect(Collectors.toList());
    }

    // ── STOCK DE UN PRODUCTO ESPECÍFICO ───────────
    public StockDTO stockPorProducto(Long productoId) {

        Producto producto = Producto.findById(productoId);
        if (producto == null || !producto.activo) {
            throw new NotFoundException("Producto no encontrado");
        }

        Kardex ultimo = Kardex
                .find("producto.id = ?1 ORDER BY creadoEn DESC", productoId)
                .firstResult();

        LocalDateTime ultimoMov = ultimo != null
                ? ultimo.creadoEn : producto.creadoEn;

        return new StockDTO(producto, ultimoMov);
    }
}