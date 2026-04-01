package com.ventas.cliente.dto;

import com.ventas.cliente.Cliente;
import java.time.LocalDateTime;

// Lo que DEVOLVEMOS al frontend
public class ClienteDTO {

    public Long          id;
    public String        nombre;
    public String        email;
    public String        telefono;
    public String        direccion;
    public String        documento;
    public Boolean       activo;
    public LocalDateTime creadoEn;
    public LocalDateTime actualizadoEn;

    public ClienteDTO() {}

    // Constructor que convierte Entity → DTO
    public ClienteDTO(Cliente c) {
        this.id            = c.id;
        this.nombre        = c.nombre;
        this.email         = c.email;
        this.telefono      = c.telefono;
        this.direccion     = c.direccion;
        this.documento     = c.documento;
        this.activo        = c.activo;
        this.creadoEn      = c.creadoEn;
        this.actualizadoEn = c.actualizadoEn;
    }
}