package com.ventas.auth;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    public String token;

    @Column(name = "usuario_id", nullable = false)
    public Long usuarioId;

    @Column(name = "expira_en", nullable = false)
    public LocalDateTime expiraEn;

    @Column(name = "creado_en")
    public LocalDateTime creadoEn = LocalDateTime.now();

    @Column(nullable = false)
    public Boolean revocado = false;

    // Buscar token válido (no revocado y no expirado)
    public static RefreshToken findValido(String token) {
        return find(
                "token = ?1 AND revocado = false AND expiraEn > ?2",
                token, LocalDateTime.now()
        ).firstResult();
    }

    // Revocar todos los tokens de un usuario (logout)
    public static void revocarTodos(Long usuarioId) {
        update("revocado = true WHERE usuarioId = ?1", usuarioId);
    }
}