package com.ventas.venta;

import com.ventas.cliente.Cliente;
import com.ventas.common.PageResponse;
import com.ventas.kardex.Kardex;
import com.ventas.producto.Producto;
import com.ventas.usuario.Usuario;
import com.ventas.venta.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class VentaService
{

    @ConfigProperty(name = "app.igv.porcentaje")
    BigDecimal igvPorcentaje;

    // ── LISTAR ────────────────────────────────────
    public PageResponse<VentaDTO> listar(int page, int size, String estado) {

        String query;
        Object[] params;

        if (estado != null && !estado.isBlank()) {
            query  = "estado = ?1 ORDER BY fecha DESC";
            params = new Object[]{estado};
        } else {
            query  = "1=1 ORDER BY fecha DESC";
            params = new Object[]{};
        }

        long total = Venta.count(
                estado != null && !estado.isBlank()
                        ? "estado = ?1" : "1=1", params
        );

        List<VentaDTO> data = Venta
                .find(query, params)
                .page(page, size)
                .list()
                .stream()
                .map(v -> new VentaDTO((Venta) v))
                .collect(Collectors.toList());

        return new PageResponse<>(data, total, page, size);
    }

    // ── BUSCAR POR ID ─────────────────────────────
    public VentaDTO buscarPorId(Long id) {
        Venta venta = Venta.findById(id);

        if (venta == null) {
            throw new NotFoundException("Venta no encontrada");
        }

        return new VentaDTO(venta);
    }

    // ── REGISTRAR VENTA ───────────────────────────
    @Transactional
    public VentaDTO registrar(VentaRequest request, Long usuarioId) {

        // 1. Buscar usuario
        Usuario usuario = Usuario.findById(usuarioId);
        if (usuario == null) {
            throw new NotFoundException("Usuario no encontrado");
        }

        // 2. Buscar cliente (opcional)
        Cliente cliente = null;
        if (request.clienteId != null) {
            cliente = Cliente.findById(request.clienteId);
            if (cliente == null || !cliente.activo) {
                throw new NotFoundException("Cliente no encontrado");
            }
        }

        // 3. Validar productos y stock ANTES de crear nada
        List<Producto> productos = new ArrayList<>();
        for (VentaDetalleRequest det : request.detalles) {

            Producto producto = Producto.findById(det.productoId);

            if (producto == null || !producto.activo) {
                throw new NotFoundException(
                        "Producto no encontrado: " + det.productoId
                );
            }

            if (!producto.tieneStock(det.cantidad)) {
                throw new BadRequestException(
                        "Stock insuficiente para: " + producto.nombre +
                                ". Stock actual: " + producto.stock +
                                ", solicitado: " + det.cantidad
                );
            }

            productos.add(producto);
        }

        // 4. Crear cabecera de venta
        Venta venta       = new Venta();
        venta.numero      = generarNumero();
        venta.cliente     = cliente;
        venta.usuario     = usuario;
        venta.fecha       = LocalDateTime.now();
        venta.estado      = "COMPLETADA";
        venta.observacion = request.observacion;
        venta.creadoEn    = LocalDateTime.now();
        Venta.persist(venta);

        // 5. Crear detalles + calcular totales
        BigDecimal subtotal = BigDecimal.ZERO;

        for (int i = 0; i < request.detalles.size(); i++) {
            VentaDetalleRequest detReq = request.detalles.get(i);
            Producto producto          = productos.get(i);

            // Precio viene de la BD (no del frontend) ✅
            BigDecimal precioUnitario = producto.precio;
            BigDecimal subtotalDet    = precioUnitario
                    .multiply(BigDecimal.valueOf(detReq.cantidad))
                    .setScale(2, RoundingMode.HALF_UP);

            // Crear detalle
            VentaDetalle detalle       = new VentaDetalle();
            detalle.venta              = venta;
            detalle.producto           = producto;
            detalle.cantidad           = detReq.cantidad;
            detalle.precioUnitario     = precioUnitario;
            detalle.subtotal           = subtotalDet;
            VentaDetalle.persist(detalle);

            subtotal = subtotal.add(subtotalDet);

            // 6. Descontar stock
            int stockAnterior    = producto.stock;
            producto.stock      -= detReq.cantidad;
            producto.actualizadoEn = LocalDateTime.now();

            // 7. Registrar movimiento en Kardex (OUT)
            registrarKardex(
                    producto,
                    "OUT",
                    detReq.cantidad,
                    stockAnterior,
                    producto.stock,
                    "Venta " + venta.numero,
                    venta.id,
                    usuario
            );
        }

        // 8. Calcular IGV y total
        BigDecimal igv   = subtotal
                .multiply(igvPorcentaje)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv);

        venta.subtotal  = subtotal;
        venta.impuesto  = igv;
        venta.total     = total;

        return new VentaDTO(venta);
    }

    // ── ANULAR VENTA ──────────────────────────────
    @Transactional
    public VentaDTO anular(Long id, Long usuarioId) {

        Venta venta = Venta.findById(id);

        if (venta == null) {
            throw new NotFoundException("Venta no encontrada");
        }

        if (!"COMPLETADA".equals(venta.estado)) {
            throw new BadRequestException(
                    "Solo se pueden anular ventas COMPLETADAS"
            );
        }

        Usuario usuario = Usuario.findById(usuarioId);

        // Devolver stock de cada producto
        for (VentaDetalle detalle : venta.detalles) {
            Producto producto   = detalle.producto;
            int stockAnterior   = producto.stock;
            producto.stock     += detalle.cantidad;
            producto.actualizadoEn = LocalDateTime.now();

            // Registrar devolución en Kardex (IN)
            registrarKardex(
                    producto,
                    "IN",
                    detalle.cantidad,
                    stockAnterior,
                    producto.stock,
                    "Anulacion venta " + venta.numero,
                    venta.id,
                    usuario
            );
        }

        venta.estado = "ANULADA";

        return new VentaDTO(venta);
    }

    // ── GENERAR NÚMERO CORRELATIVO ────────────────
    private String generarNumero() {
        long count = Venta.count();
        return String.format("V-%05d", count + 1);
    }

    // ── REGISTRAR KARDEX ──────────────────────────
    private void registrarKardex(
            Producto producto,
            String tipo,
            int cantidad,
            int stockAnterior,
            int stockPosterior,
            String motivo,
            Long referenciaId,
            Usuario usuario) {

        Kardex kardex           = new Kardex();
        kardex.producto         = producto;
        kardex.tipo             = tipo;
        kardex.cantidad         = cantidad;
        kardex.stockAnterior    = stockAnterior;
        kardex.stockPosterior   = stockPosterior;
        kardex.motivo           = motivo;
        kardex.referenciaId     = referenciaId;
        kardex.usuario          = usuario;
        kardex.creadoEn         = LocalDateTime.now();
        Kardex.persist(kardex);
    }
}