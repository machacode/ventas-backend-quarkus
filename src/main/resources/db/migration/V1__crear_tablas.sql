-- =====================================================
-- V1: Tablas base del sistema de ventas
-- Flyway ejecuta este archivo UNA SOLA VEZ
-- Nombre obligatorio: V{numero}__{descripcion}.sql
--                          ↑↑
--                     doble guion bajo
-- =====================================================

-- ── usuarios ────────────────────────────────────────
CREATE TABLE usuarios (
                          id             BIGSERIAL    PRIMARY KEY,
                          nombre         VARCHAR(100) NOT NULL,
                          email          VARCHAR(150) NOT NULL UNIQUE,
                          password       VARCHAR(255) NOT NULL,
                          rol            VARCHAR(20)  NOT NULL DEFAULT 'VENDEDOR',
                          activo         BOOLEAN      NOT NULL DEFAULT TRUE,
                          creado_en      TIMESTAMP    NOT NULL DEFAULT NOW(),
                          actualizado_en TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── clientes ────────────────────────────────────────
CREATE TABLE clientes (
                          id             BIGSERIAL    PRIMARY KEY,
                          nombre         VARCHAR(100) NOT NULL,
                          email          VARCHAR(150),
                          telefono       VARCHAR(20),
                          direccion      VARCHAR(255),
                          documento      VARCHAR(20)  UNIQUE,
                          activo         BOOLEAN      NOT NULL DEFAULT TRUE,
                          creado_en      TIMESTAMP    NOT NULL DEFAULT NOW(),
                          actualizado_en TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ── productos ───────────────────────────────────────
CREATE TABLE productos (
                           id             BIGSERIAL      PRIMARY KEY,
                           codigo         VARCHAR(50)    NOT NULL UNIQUE,
                           nombre         VARCHAR(150)   NOT NULL,
                           descripcion    TEXT,
                           precio         DECIMAL(10,2)  NOT NULL,
                           stock          INTEGER        NOT NULL DEFAULT 0,
                           stock_minimo   INTEGER        NOT NULL DEFAULT 0,
                           unidad         VARCHAR(20)    NOT NULL DEFAULT 'UND',
                           activo         BOOLEAN        NOT NULL DEFAULT TRUE,
                           creado_en      TIMESTAMP      NOT NULL DEFAULT NOW(),
                           actualizado_en TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── ventas (cabecera) ────────────────────────────────
CREATE TABLE ventas (
                        id           BIGSERIAL      PRIMARY KEY,
                        numero       VARCHAR(20)    NOT NULL UNIQUE,
                        cliente_id   BIGINT         REFERENCES clientes(id),
                        usuario_id   BIGINT         NOT NULL REFERENCES usuarios(id),
                        fecha        TIMESTAMP      NOT NULL DEFAULT NOW(),
                        subtotal     DECIMAL(10,2)  NOT NULL DEFAULT 0,
                        impuesto     DECIMAL(10,2)  NOT NULL DEFAULT 0,
                        total        DECIMAL(10,2)  NOT NULL DEFAULT 0,
                        estado       VARCHAR(20)    NOT NULL DEFAULT 'COMPLETADA',
                        observacion  TEXT,
                        creado_en    TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── venta_detalles (líneas de cada venta) ────────────
CREATE TABLE venta_detalles (
                                id              BIGSERIAL      PRIMARY KEY,
                                venta_id        BIGINT         NOT NULL REFERENCES ventas(id),
                                producto_id     BIGINT         NOT NULL REFERENCES productos(id),
                                cantidad        INTEGER        NOT NULL,
                                precio_unitario DECIMAL(10,2)  NOT NULL,
                                subtotal        DECIMAL(10,2)  NOT NULL
);

-- ── kardex (movimientos de inventario) ───────────────
CREATE TABLE kardex (
                        id              BIGSERIAL   PRIMARY KEY,
                        producto_id     BIGINT      NOT NULL REFERENCES productos(id),
                        tipo            VARCHAR(10) NOT NULL,
                        cantidad        INTEGER     NOT NULL,
                        stock_anterior  INTEGER     NOT NULL,
                        stock_posterior INTEGER     NOT NULL,
                        motivo          VARCHAR(100),
                        referencia_id   BIGINT,
                        usuario_id      BIGINT      REFERENCES usuarios(id),
                        creado_en       TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ── índices para búsquedas rápidas ───────────────────
CREATE INDEX idx_ventas_fecha    ON ventas(fecha);
CREATE INDEX idx_ventas_cliente  ON ventas(cliente_id);
CREATE INDEX idx_kardex_producto ON kardex(producto_id);
CREATE INDEX idx_kardex_fecha    ON kardex(creado_en);
CREATE INDEX idx_productos_cod   ON productos(codigo);