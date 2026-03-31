package com.ventas.auth;

import com.ventas.auth.dto.AuthResponse;
import com.ventas.auth.dto.LoginRequest;
import com.ventas.auth.dto.RegisterRequest;
import com.ventas.usuario.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAuthorizedException;

import java.time.LocalDateTime;

@ApplicationScoped
public class AuthService {

    @Inject
    PasswordService passwordService;

    @Inject
    TokenService tokenService;

    // ── LOGIN ─────────────────────────────────────
    @Transactional
    public AuthResponse login(LoginRequest request) {

        // 1. Buscar usuario
        Usuario usuario = Usuario.findByEmail(request.email);
        if (usuario == null) {
            // Mismo mensaje para no revelar si el email existe
            throw new NotAuthorizedException("Credenciales incorrectas");
        }

        // 2. Verificar contraseña
        if (!passwordService.verify(request.password, usuario.password)) {
            throw new NotAuthorizedException("Credenciales incorrectas");
        }

        // 3. Verificar que esté activo
        if (!usuario.activo) {
            throw new BadRequestException("Usuario desactivado");
        }

        return buildResponse(usuario);
    }

    // ── REGISTER ──────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // 1. Verificar email único
        if (Usuario.findByEmail(request.email) != null) {
            throw new BadRequestException("El email ya esta registrado");
        }

        // 2. Crear usuario
        Usuario nuevo = new Usuario();
        nuevo.nombre   = request.nombre;
        nuevo.email    = request.email;
        nuevo.password = passwordService.hash(request.password);
        nuevo.rol      = "VENDEDOR";
        nuevo.activo   = true;
        Usuario.persist(nuevo);

        return buildResponse(nuevo);
    }

    // ── REFRESH ───────────────────────────────────
    @Transactional
    public AuthResponse refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("Refresh token requerido");
        }

        // 1. Buscar token válido en BD
        RefreshToken stored = RefreshToken.findValido(refreshToken);
        if (stored == null) {
            throw new NotAuthorizedException("Refresh token invalido o expirado");
        }

        // 2. Buscar usuario
        Usuario usuario = Usuario.findById(stored.usuarioId);
        if (usuario == null || !usuario.activo) {
            throw new NotAuthorizedException("Usuario no valido");
        }

        // 3. Revocar token usado (rotación de tokens)
        stored.revocado = true;

        // 4. Generar nuevos tokens
        return buildResponse(usuario);
    }

    // ── LOGOUT ────────────────────────────────────
    @Transactional
    public void logout(Long usuarioId) {
        RefreshToken.revocarTodos(usuarioId);
    }

    // ── HELPER: construir respuesta ───────────────
    private AuthResponse buildResponse(Usuario usuario) {

        String accessToken  = tokenService.generateAccessToken(usuario);
        String refreshToken = tokenService.generateRefreshToken(usuario);

        // Guardar refresh token en BD
        RefreshToken rt  = new RefreshToken();
        rt.token         = refreshToken;
        rt.usuarioId     = usuario.id;
        rt.expiraEn      = LocalDateTime.now().plusSeconds(tokenService.getRefreshDuration());
        rt.revocado      = false;
        RefreshToken.persist(rt);

        return new AuthResponse(
                accessToken,
                tokenService.getAccessDuration(),
                usuario.id,
                usuario.nombre,
                usuario.email,
                usuario.rol
        );
    }
}