-- Plantillas de movimientos recurrentes (alquiler, suscripción, etc).
-- Un job @Scheduled diario materializa el movimiento real cuando llega
-- proxima_ejecucion y avanza la fecha según la frecuencia.
CREATE TABLE movimientos_recurrentes (
    id                BIGSERIAL     PRIMARY KEY,
    usuario_id        BIGINT        NOT NULL REFERENCES usuarios(id)   ON DELETE CASCADE,
    categoria_id      BIGINT                 REFERENCES categorias(id) ON DELETE SET NULL,
    tipo              VARCHAR(10)   NOT NULL CHECK (tipo IN ('GASTO','INGRESO')),
    importe           NUMERIC(12,2) NOT NULL CHECK (importe > 0),
    descripcion       VARCHAR(200),
    frecuencia        VARCHAR(10)   NOT NULL CHECK (frecuencia IN ('DIARIA','SEMANAL','MENSUAL','ANUAL')),
    proxima_ejecucion DATE          NOT NULL,
    activo            BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_recurrentes_usuario      ON movimientos_recurrentes (usuario_id);
CREATE INDEX idx_recurrentes_activos_due  ON movimientos_recurrentes (activo, proxima_ejecucion);
