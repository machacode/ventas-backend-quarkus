package com.ventas.kardex.dto;

import com.ventas.kardex.Kardex;
import java.time.LocalDateTime;

public class KardexDTO
{

    public Long          id;
    public ProductoInfo  producto;
    public String        tipo;
    public Integer       cantidad;
    public Integer       stockAnterior;
    public Integer       stockPosterior;
    public String        motivo;
    public Long          referenciaId;
    public UsuarioInfo   usuario;
    public LocalDateTime creadoEn;

    public static class ProductoInfo {
        public Long   id;
        public String codigo;
        public String nombre;

        public ProductoInfo(Long id, String codigo, String nombre) {
            this.id     = id;
            this.codigo = codigo;
            this.nombre = nombre;
        }
    }

    public static class UsuarioInfo {
        public Long   id;
        public String nombre;

        public UsuarioInfo(Long id, String nombre) {
            this.id     = id;
            this.nombre = nombre;
        }
    }

    public KardexDTO() {}

    public KardexDTO(Kardex k) {
        this.id             = k.id;
        this.tipo           = k.tipo;
        this.cantidad       = k.cantidad;
        this.stockAnterior  = k.stockAnterior;
        this.stockPosterior = k.stockPosterior;
        this.motivo         = k.motivo;
        this.referenciaId   = k.referenciaId;
        this.creadoEn       = k.creadoEn;

        if (k.producto != null) {
            this.producto = new ProductoInfo(
                    k.producto.id,
                    k.producto.codigo,
                    k.producto.nombre
            );
        }

        if (k.usuario != null) {
            this.usuario = new UsuarioInfo(
                    k.usuario.id,
                    k.usuario.nombre
            );
        }
    }
}