package com.ventas.common;

import java.util.List;

// Respuesta paginada genérica
// El <T> significa que funciona para cualquier tipo:
// PageResponse<ClienteDTO>, PageResponse<ProductoDTO>, etc.

public class PageResponse<T> {

    public List<T> data;        // lista de registros
    public long    total;       // total de registros en BD
    public int     page;        // página actual (empieza en 0)
    public int     size;        // registros por página
    public int     totalPages;  // total de páginas

    public PageResponse() {}

    public PageResponse(List<T> data, long total, int page, int size) {
        this.data       = data;
        this.total      = total;
        this.page       = page;
        this.size       = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
}