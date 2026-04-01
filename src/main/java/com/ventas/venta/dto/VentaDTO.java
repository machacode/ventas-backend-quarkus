package com.ventas.venta.dto;

import com.ventas.venta.Venta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class VentaDTO {

    public Long              id;
    public String            numero;
    public ClienteInfo       cliente;
    public UsuarioInfo       usuario;
    public LocalDateTime     fecha;
    public BigDecimal        subtotal;
    public BigDecimal        igv;
    public BigDecimal        total;
    public String            estado;
    public String            observacion;
    public List<VentaDetalleDTO> detalles;
    public LocalDateTime     creadoEn;

    // Info resumida del cliente
    public static class ClienteInfo
    {
        public Long   id;
        public String nombre;
        public String documento;

        public ClienteInfo(Long id, String nombre, String documento) {
            this.id       = id;
            this.nombre   = nombre;
            this.documento = documento;
        }
    }

    // Info resumida del usuario
    public static class UsuarioInfo {
        public Long   id;
        public String nombre;
        public String email;

        public UsuarioInfo(Long id, String nombre, String email) {
            this.id     = id;
            this.nombre = nombre;
            this.email  = email;
        }
    }

    public VentaDTO() {}

    public VentaDTO(Venta v) {
        this.id          = v.id;
        this.numero      = v.numero;
        this.fecha       = v.fecha;
        this.subtotal    = v.subtotal;
        this.igv         = v.impuesto;
        this.total       = v.total;
        this.estado      = v.estado;
        this.observacion = v.observacion;
        this.creadoEn    = v.creadoEn;

        // Cliente (puede ser nulo — venta sin cliente)
        if (v.cliente != null) {
            this.cliente = new ClienteInfo(
                    v.cliente.id,
                    v.cliente.nombre,
                    v.cliente.documento
            );
        }

        // Usuario que registró la venta
        if (v.usuario != null) {
            this.usuario = new UsuarioInfo(
                    v.usuario.id,
                    v.usuario.nombre,
                    v.usuario.email
            );
        }

        // Detalles
        if (v.detalles != null) {
            this.detalles = v.detalles.stream()
                    .map(VentaDetalleDTO::new)
                    .collect(Collectors.toList());
        }
    }
}