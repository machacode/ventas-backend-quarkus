package com.ventas.kardex.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class KardexRequest
{

    @NotNull(message = "El productoId es obligatorio")
    public Long productoId;

    @NotNull(message = "El tipo es obligatorio")
    @Pattern(
            regexp = "IN|OUT|ADJUST",
            message = "El tipo debe ser IN, OUT o ADJUST"
    )
    public String tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad minima es 1")
    public Integer cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    public String motivo;
}