package com.ventas.venta;

import com.ventas.cliente.Cliente;
import com.ventas.usuario.Usuario;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class Venta extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 20)
    public String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    public Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;

    @Column(nullable = false)
    public LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal impuesto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal total = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    public String estado = "COMPLETADA";

    public String observacion;

    @Column(name = "creado_en")
    public LocalDateTime creadoEn = LocalDateTime.now();

    // Una venta tiene muchos detalles
    @OneToMany(
            mappedBy = "venta",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    public List<VentaDetalle> detalles = new ArrayList<>();
}