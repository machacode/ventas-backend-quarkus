package com.ventas.venta.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class VentaDetalleRequest
{

    @NotNull(message = "El productoId es obligatorio")
    public Long productoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad minima es 1")
    public Integer cantidad;
}