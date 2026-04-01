package com.ventas.kardex.dto;

import com.ventas.producto.Producto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class StockDTO
{

    public Long          productoId;
    public String        codigo;
    public String        nombre;
    public Integer       stockActual;
    public Integer       stockMinimo;
    public Boolean       stockBajo;
    public BigDecimal    precio;
    public String        unidad;
    public LocalDateTime ultimoMovimiento;

    public StockDTO() {}

    public StockDTO(Producto p, LocalDateTime ultimoMovimiento) {
        this.productoId       = p.id;
        this.codigo           = p.codigo;
        this.nombre           = p.nombre;
        this.stockActual      = p.stock;
        this.stockMinimo      = p.stockMinimo;
        this.stockBajo        = p.stock <= p.stockMinimo;
        this.precio           = p.precio;
        this.unidad           = p.unidad;
        this.ultimoMovimiento = ultimoMovimiento;
    }
}