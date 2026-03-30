package com.ventas.venta;

import com.ventas.producto.Producto;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "venta_detalles")
public class VentaDetalle extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    public Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    public Producto producto;

    @Column(nullable = false)
    public Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    public BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal subtotal;
}