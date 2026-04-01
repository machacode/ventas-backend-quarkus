package com.ventas.venta.dto;

import com.ventas.venta.VentaDetalle;
import java.math.BigDecimal;

public class VentaDetalleDTO
{

    public Long       id;
    public Long       productoId;
    public String     productoNombre;
    public String     productoCodigo;
    public Integer    cantidad;
    public BigDecimal precioUnitario;
    public BigDecimal subtotal;

    public VentaDetalleDTO() {}

    public VentaDetalleDTO(VentaDetalle d) {
        this.id             = d.id;
        this.productoId     = d.producto.id;
        this.productoNombre = d.producto.nombre;
        this.productoCodigo = d.producto.codigo;
        this.cantidad       = d.cantidad;
        this.precioUnitario = d.precioUnitario;
        this.subtotal       = d.subtotal;
    }
}