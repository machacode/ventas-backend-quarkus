package com.ventas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es valido")
    public String email;

    @NotBlank(message = "La contrasena es obligatoria")
    public String password;
}