package com.ventas.usuario;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String nombre;

    @Column(nullable = false, unique = true, length = 150)
    public String email;

    @Column(nullable = false)
    public String password;   // siempre guardamos el HASH, nunca texto plano

    @Column(nullable = false, length = 20)
    public String rol = "VENDEDOR";

    @Column(nullable = false)
    public Boolean activo = true;

    @Column(name = "creado_en")
    public LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en")
    public LocalDateTime actualizadoEn = LocalDateTime.now();

    // Buscar usuario por email (para el login)
    public static Usuario findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public static java.util.List<Usuario> findActivos() {
        return list("activo", true);
    }
}