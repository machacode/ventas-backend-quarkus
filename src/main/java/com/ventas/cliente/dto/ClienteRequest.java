package com.ventas.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Maximo 100 caracteres")
    public String nombre;

    @Email(message = "El email no es valido")
    public String email;

    @Size(max = 20, message = "Maximo 20 caracteres")
    public String telefono;

    @Size(max = 255, message = "Maximo 255 caracteres")
    public String direccion;

    @Size(max = 20, message = "Maximo 20 caracteres")
    public String documento;
}
