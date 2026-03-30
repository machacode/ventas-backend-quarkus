package com.ventas.producto;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "productos")
public class Producto extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 50)
    public String codigo;

    @Column(nullable = false, length = 150)
    public String nombre;

    @Column(columnDefinition = "TEXT")
    public String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal precio;

    @Column(nullable = false)
    public Integer stock = 0;

    @Column(name = "stock_minimo", nullable = false)
    public Integer stockMinimo = 0;

    @Column(nullable = false, length = 20)
    public String unidad = "UND";

    @Column(nullable = false)
    public Boolean activo = true;

    @Column(name = "creado_en")
    public LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en")
    public LocalDateTime actualizadoEn = LocalDateTime.now();

    public static Producto findByCodigo(String codigo) {
        return find("codigo", codigo).firstResult();
    }

    public static List<Producto> findActivos() {
        return list("activo", true);
    }

    // Verifica si hay suficiente stock para una venta
    public boolean tieneStock(int cantidad) {
        return this.stock >= cantidad;
    }
}