-- Tabla de usuarios autenticables.
CREATE TABLE usuarios (
    id              BIGSERIAL    PRIMARY KEY,
    email           VARCHAR(254) NOT NULL UNIQUE,
    nombre          VARCHAR(100) NOT NULL,
    password_hash   VARCHAR(72)  NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuarios_email ON usuarios (LOWER(email));

-- Tokens de reseteo de contraseña (uso único, con expiración).
CREATE TABLE password_reset_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token       VARCHAR(64)  NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ  NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens (token);
CREATE INDEX idx_password_reset_tokens_usuario ON password_reset_tokens (usuario_id);
