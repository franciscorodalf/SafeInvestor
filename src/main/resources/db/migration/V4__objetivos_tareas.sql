-- Objetivos de ahorro por usuario.
CREATE TABLE objetivos (
    id                BIGSERIAL     PRIMARY KEY,
    usuario_id        BIGINT        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre            VARCHAR(100)  NOT NULL,
    importe_objetivo  NUMERIC(12,2) NOT NULL CHECK (importe_objetivo > 0),
    importe_ahorrado  NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (importe_ahorrado >= 0),
    fecha_limite      DATE,
    color             VARCHAR(7)    NOT NULL DEFAULT '#3B82F6',
    completado_at     TIMESTAMPTZ,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_objetivos_usuario ON objetivos (usuario_id);

-- Tareas financieras por usuario.
CREATE TABLE tareas (
    id                BIGSERIAL    PRIMARY KEY,
    usuario_id        BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    titulo            VARCHAR(150) NOT NULL,
    descripcion       VARCHAR(500),
    fecha_vencimiento DATE,
    completada_at     TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tareas_usuario ON tareas (usuario_id);
