package com.ventas.kardex;

import com.ventas.producto.Producto;
import com.ventas.usuario.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "kardex")
public class Kardex extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    public Producto producto;

    // IN=entrada, OUT=salida, ADJUST=ajuste manual
    @Column(nullable = false, length = 10)
    public String tipo;

    @Column(nullable = false)
    public Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    public Integer stockAnterior;

    @Column(name = "stock_posterior", nullable = false)
    public Integer stockPosterior;

    @Column(length = 100)
    public String motivo;

    @Column(name = "referencia_id")
    public Long referenciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

    @Column(name = "creado_en")
    public LocalDateTime creadoEn = LocalDateTime.now();

    public static List<Kardex> findByProductoYFechas(
            Long productoId,
            LocalDateTime desde,
            LocalDateTime hasta) {
        return list(
                "producto.id = ?1 AND creadoEn >= ?2 AND creadoEn <= ?3 ORDER BY creadoEn DESC",
                productoId, desde, hasta
        );
    }
}