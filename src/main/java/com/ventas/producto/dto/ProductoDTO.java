package com.ventas.producto.dto;

import com.ventas.producto.Producto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductoDTO {

    public Long          id;
    public String        codigo;
    public String        nombre;
    public String        descripcion;
    public BigDecimal    precio;
    public Integer       stock;
    public Integer       stockMinimo;
    public String        unidad;
    public Boolean       activo;
    public Boolean       stockBajo;      // stock <= stockMinimo
    public LocalDateTime creadoEn;
    public LocalDateTime actualizadoEn;

    public ProductoDTO() {}

    public ProductoDTO(Producto p) {
        this.id            = p.id;
        this.codigo        = p.codigo;
        this.nombre        = p.nombre;
        this.descripcion   = p.descripcion;
        this.precio        = p.precio;
        this.stock         = p.stock;
        this.stockMinimo   = p.stockMinimo;
        this.unidad        = p.unidad;
        this.activo        = p.activo;
        this.stockBajo     = p.stock <= p.stockMinimo;
        this.creadoEn      = p.creadoEn;
        this.actualizadoEn = p.actualizadoEn;
    }
}