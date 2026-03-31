package com.ventas.auth.dto;

public class AuthResponse {
    public String accessToken;
    public String tokenType  = "Bearer";
    public Long   expiresIn;        // segundos
    public UsuarioInfo usuario;

    public static class UsuarioInfo {
        public Long   id;
        public String nombre;
        public String email;
        public String rol;

        public UsuarioInfo(Long id, String nombre,
                           String email, String rol) {
            this.id     = id;
            this.nombre = nombre;
            this.email  = email;
            this.rol    = rol;
        }
    }

    public AuthResponse() {}

    public AuthResponse(String accessToken, Long expiresIn,
                        Long id, String nombre,
                        String email, String rol) {
        this.accessToken = accessToken;
        this.expiresIn   = expiresIn;
        this.usuario     = new UsuarioInfo(id, nombre, email, rol);
    }
}