package com.ventas.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "Entre 2 y 100 caracteres")
    public String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es valido")
    public String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 6, message = "Minimo 6 caracteres")
    public String password;
}