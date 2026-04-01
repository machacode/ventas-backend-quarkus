package com.ventas.producto.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductoRequest {

    @NotBlank(message = "El codigo es obligatorio")
    @Size(max = 50, message = "Maximo 50 caracteres")
    public String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "Maximo 150 caracteres")
    public String nombre;

    public String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    public BigDecimal precio;

    @Min(value = 0, message = "El stock no puede ser negativo")
    public Integer stock = 0;

    @Min(value = 0, message = "El stock minimo no puede ser negativo")
    public Integer stockMinimo = 0;

    @Size(max = 20, message = "Maximo 20 caracteres")
    public String unidad = "UND";
}