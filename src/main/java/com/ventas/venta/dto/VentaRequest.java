package com.ventas.venta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class VentaRequest
{

    public Long   clienteId;    // opcional
    public String observacion;  // opcional

    @NotNull(message = "Los detalles son obligatorios")
    @NotEmpty(message = "Debe tener al menos un producto")
    @Valid
    public List<VentaDetalleRequest> detalles;
}