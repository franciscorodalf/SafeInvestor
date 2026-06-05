-- Categorías por usuario.
CREATE TABLE categorias (
    id          BIGSERIAL    PRIMARY KEY,
    usuario_id  BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre      VARCHAR(50)  NOT NULL,
    color       VARCHAR(7)   NOT NULL DEFAULT '#6B7280',
    icono       VARCHAR(50),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE (usuario_id, nombre)
);

CREATE INDEX idx_categorias_usuario ON categorias (usuario_id);

-- Movimientos (gastos e ingresos).
CREATE TABLE movimientos (
    id           BIGSERIAL     PRIMARY KEY,
    usuario_id   BIGINT        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    categoria_id BIGINT        REFERENCES categorias(id) ON DELETE SET NULL,
    tipo         VARCHAR(10)   NOT NULL CHECK (tipo IN ('GASTO', 'INGRESO')),
    importe      NUMERIC(12,2) NOT NULL CHECK (importe > 0),
    descripcion  VARCHAR(200),
    fecha        DATE          NOT NULL,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_movimientos_usuario_fecha ON movimientos (usuario_id, fecha DESC);
CREATE INDEX idx_movimientos_categoria ON movimientos (categoria_id);
