-- Presupuesto mensual por categoría: cuánto el usuario quiere no superar
-- en una categoría concreta durante un mes concreto.
CREATE TABLE presupuestos (
    id            BIGSERIAL     PRIMARY KEY,
    usuario_id    BIGINT        NOT NULL REFERENCES usuarios(id)   ON DELETE CASCADE,
    categoria_id  BIGINT        NOT NULL REFERENCES categorias(id) ON DELETE CASCADE,
    anio          INTEGER       NOT NULL CHECK (anio  BETWEEN 2000 AND 2200),
    mes           INTEGER       NOT NULL CHECK (mes   BETWEEN 1    AND 12),
    limite        NUMERIC(12,2) NOT NULL CHECK (limite > 0),
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    -- Un usuario solo puede tener un presupuesto por (categoria, año, mes)
    CONSTRAINT presupuestos_unique UNIQUE (usuario_id, categoria_id, anio, mes)
);

CREATE INDEX idx_presupuestos_usuario_periodo
    ON presupuestos (usuario_id, anio, mes);
