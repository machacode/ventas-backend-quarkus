package com.ventas.auth;

import com.ventas.auth.dto.AuthResponse;
import com.ventas.auth.dto.LoginRequest;
import com.ventas.auth.dto.RegisterRequest;
import com.ventas.usuario.Usuario;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject AuthService  authService;
    @Inject JsonWebToken jwt;

    // ── POST /api/auth/register ───────────────────
    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid RegisterRequest request)
    {
        AuthResponse response = authService.register(request);
        return Response.status(201).entity(response).build();
    }

    // ── POST /api/auth/login ──────────────────────
    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request, @Context UriInfo uriInfo)
    {
        AuthResponse response = authService.login(request);

        // Refresh token en cookie httpOnly
        NewCookie cookie = buildRefreshCookie(
                response.accessToken,   // placeholder hasta integrar
                uriInfo.getBaseUri().getHost(),
                tokenService().getRefreshDuration().intValue() //(int) tokenService().getRefreshDuration()
        );

        return Response.ok(response).cookie(cookie).build();
    }

    // ── POST /api/auth/refresh ────────────────────
    @POST
    @Path("/refresh")
    @PermitAll
    public Response refresh(@CookieParam("refreshToken") String refreshToken, @Context UriInfo uriInfo)
    {
        AuthResponse response = authService.refresh(refreshToken);

        NewCookie cookie = buildRefreshCookie(
                response.accessToken,
                uriInfo.getBaseUri().getHost(),
                tokenService().getRefreshDuration().intValue() //(int) tokenService().getRefreshDuration()
        );

        return Response.ok(response).cookie(cookie).build();
    }

    // ── POST /api/auth/logout ─────────────────────
    @POST
    @Path("/logout")
    @RolesAllowed({"ADMIN", "VENDEDOR"})
    public Response logout(@Context UriInfo uriInfo)
    {

        Long usuarioId = Long.parseLong(jwt.getSubject());
        authService.logout(usuarioId);

        // Borrar cookie
        NewCookie deleteCookie = buildRefreshCookie(
                "", uriInfo.getBaseUri().getHost(), 0
        );

        return Response.ok("{\"mensaje\":\"Sesion cerrada\"}")
                .cookie(deleteCookie)
                .build();
    }

    // ── GET /api/auth/me ──────────────────────────
    @GET
    @Path("/me")
    @RolesAllowed({"ADMIN", "VENDEDOR"})
    public Usuario me()
    {
        Long usuarioId = Long.parseLong(jwt.getSubject());
        Usuario usuario = Usuario.findById(usuarioId);
        usuario.password = null;   // nunca devolver el hash
        return usuario;
    }

    // ── Helper: construir cookie httpOnly ─────────
    private NewCookie buildRefreshCookie(String value, String domain, int maxAge)
    {
        return new NewCookie.Builder("refreshToken")
                .value(value)
                .path("/api/auth")   // solo rutas de auth
                .domain(domain)
                .maxAge(maxAge)
                .httpOnly(true)      // JS no puede leerla ✅
                .secure(false)       // true en producción (HTTPS)
                .sameSite(NewCookie.SameSite.STRICT)
                .build();
    }

    // Helper para acceder a TokenService sin inyección extra
    @Inject TokenService tokenService;
    private TokenService tokenService() { return tokenService; }
}