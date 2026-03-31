package com.ventas.auth;

import com.ventas.usuario.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "app.token.access.duration")
    Long accessDuration;

    @ConfigProperty(name = "app.token.refresh.duration")
    Long refreshDuration;

    // ── Access Token (1 hora) ─────────────────────
    public String generateAccessToken(Usuario usuario) {
        return Jwt.issuer(issuer)
                .subject(usuario.id.toString())
                .claim("email", usuario.email)
                .claim("nombre", usuario.nombre)
                .groups(new HashSet<>(Arrays.asList(usuario.rol)))
                //.claim("roles", new HashSet<>(Arrays.asList(usuario.rol)))
                .claim("type", "access")
                .expiresIn(Duration.ofSeconds(accessDuration))
                .sign();
    }

    // ── Refresh Token (7 días) ────────────────────
    public String generateRefreshToken(Usuario usuario) {
        return Jwt.issuer(issuer)
                .subject(usuario.id.toString())
                .claim("type", "refresh")
                .expiresIn(Duration.ofSeconds(refreshDuration))
                .sign();
    }

    public Long getAccessDuration()  { return accessDuration;  }
    public Long getRefreshDuration() { return refreshDuration; }
}