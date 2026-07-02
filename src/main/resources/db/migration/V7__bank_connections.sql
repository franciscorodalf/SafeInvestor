-- Fase 9: conexiones bancarias vía GoCardless (Nordigen).
--
-- Una conexión = un consentimiento del usuario para leer un banco durante
-- 90 días (default de PSD2). Cada conexión descubre una o más cuentas.
-- Cada movimiento importado guarda el transactionId del banco en
-- movimientos.external_id para poder dedup en syncs sucesivos.

CREATE TABLE bank_connections (
    id                BIGSERIAL     PRIMARY KEY,
    usuario_id        BIGINT        NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    -- IDs de GoCardless
    requisition_id    VARCHAR(64)   NOT NULL,
    institution_id    VARCHAR(64)   NOT NULL,
    -- Datos cacheados para no volver a llamar a la API solo para mostrar
    institution_name  VARCHAR(200)  NOT NULL,
    institution_logo  VARCHAR(500),
    -- Ciclo de vida
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
        -- PENDING (creada, esperando autenticación del usuario en el banco)
        -- LINKED  (usuario autorizó, tenemos cuentas visibles)
        -- EXPIRED (los 90 días caducaron, hay que renovar)
        -- REVOKED (borrada manualmente por el usuario)
    linked_at         TIMESTAMPTZ,
    expires_at        TIMESTAMPTZ,
    last_sync_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT bank_connections_req_unique UNIQUE (usuario_id, requisition_id)
);

CREATE INDEX idx_bank_connections_usuario ON bank_connections (usuario_id);
CREATE INDEX idx_bank_connections_status  ON bank_connections (status);

-- Cuentas descubiertas dentro de una conexión.
-- Un banco puede exponer varias cuentas (corriente, ahorro, tarjetas...).
CREATE TABLE bank_accounts (
    id             BIGSERIAL     PRIMARY KEY,
    connection_id  BIGINT        NOT NULL REFERENCES bank_connections(id) ON DELETE CASCADE,
    external_id    VARCHAR(64)   NOT NULL,
    iban           VARCHAR(50),
    nombre         VARCHAR(200),
    currency       VARCHAR(3),
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT bank_accounts_external_unique UNIQUE (external_id)
);

CREATE INDEX idx_bank_accounts_connection ON bank_accounts (connection_id);

-- Dedup de movimientos importados por banco.
-- Único por (usuario, external_id) porque el mismo transactionId de un banco
-- puede colisionar en teoría con otro banco distinto — es paranoia razonable.
ALTER TABLE movimientos
    ADD COLUMN external_id VARCHAR(200);

CREATE UNIQUE INDEX idx_movimientos_external_id
    ON movimientos (usuario_id, external_id)
    WHERE external_id IS NOT NULL;
