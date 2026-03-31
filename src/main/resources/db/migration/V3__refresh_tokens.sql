-- =====================================================
-- V3: Tabla para almacenar refresh tokens
-- =====================================================
CREATE TABLE refresh_tokens (
                                id         BIGSERIAL    PRIMARY KEY,
                                token      VARCHAR(500) NOT NULL UNIQUE,
                                usuario_id BIGINT       NOT NULL REFERENCES usuarios(id),
                                expira_en  TIMESTAMP    NOT NULL,
                                creado_en  TIMESTAMP    NOT NULL DEFAULT NOW(),
                                revocado   BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_refresh_token       ON refresh_tokens(token);
CREATE INDEX idx_refresh_usuario     ON refresh_tokens(usuario_id);