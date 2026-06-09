# SafeInvestor v2 — Phase 3: Objetivos de ahorro + Tareas financieras

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Two new features paralelas:
1. **Objetivos de ahorro** — "quiero ahorrar 2.000€ para vacaciones antes de diciembre". CRUD + endpoint para "aportar" cantidad al objetivo; cuando se alcanza, se marca como completado.
2. **Tareas financieras** — "pagar alquiler antes del día 5", "revisar nómina". CRUD + completar/descompletar. Filtros: pendientes, completadas, vencidas.

Web (Thymeleaf) + REST API para ambas. Despliegue actualizado en Render.

**Architecture:** Mismo patrón que Fase 2 — dos paquetes nuevos (`objetivos`, `tareas`) cada uno con `domain/`, `service/`, `api/`, `web/`. Reutilizar el `CurrentUser` helper de Fase 2. Sin linking entre objetivos y movimientos (futuro).

**Out of scope:** notificaciones, recordatorios por email, recurrencias automáticas de tareas, vinculación movimiento→objetivo.

---

## File Structure (added)

```
src/main/java/.../objetivos/
├── domain/{Objetivo, ObjetivoRepository}.java
├── service/ObjetivoService.java
├── api/{ObjetivoApiController, dto/{ObjetivoRequest, ObjetivoResponse, AportarRequest}}.java
└── web/{ObjetivoWebController, form/ObjetivoForm}.java

src/main/java/.../tareas/
├── domain/{Tarea, TareaRepository}.java
├── service/TareaService.java
├── api/{TareaApiController, dto/{TareaRequest, TareaResponse}}.java
└── web/{TareaWebController, form/TareaForm}.java

src/main/resources/
├── db/migration/V4__objetivos_tareas.sql
└── templates/
    ├── objetivos/list.html
    ├── objetivos/form.html
    ├── objetivos/detalle.html
    └── tareas/list.html
```

## Tasks (compactas)

### F3.1 — Migración V4

```sql
CREATE TABLE objetivos (
    id              BIGSERIAL    PRIMARY KEY,
    usuario_id      BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    nombre          VARCHAR(100) NOT NULL,
    importe_objetivo NUMERIC(12,2) NOT NULL CHECK (importe_objetivo > 0),
    importe_ahorrado NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (importe_ahorrado >= 0),
    fecha_limite    DATE,
    color           VARCHAR(7)   NOT NULL DEFAULT '#3B82F6',
    completado_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_objetivos_usuario ON objetivos (usuario_id);

CREATE TABLE tareas (
    id              BIGSERIAL    PRIMARY KEY,
    usuario_id      BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    titulo          VARCHAR(150) NOT NULL,
    descripcion     VARCHAR(500),
    fecha_vencimiento DATE,
    completada_at   TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tareas_usuario ON tareas (usuario_id);
```

### F3.2 — Objetivo entity + repo
Campos según la migración. Método helper `getPorcentaje()` que devuelve `(ahorrado / objetivo) * 100` capado a 100. Método `isCompletado()`. Repository con `findByUsuarioOrderByCompletadoAtNullsFirstFechaLimite`.

### F3.3 — Tarea entity + repo
Campos según la migración. Repository: `findByUsuarioOrderByCompletadaAtNullsFirstFechaVencimiento` o equivalente con `@Query` ordenando pendientes primero.

### F3.4 — ObjetivoService
- `findAll(usuario)`, `get(usuario, id)`
- `create(usuario, nombre, importeObjetivo, fechaLimite, color)`
- `update(usuario, id, ...)`
- `aportar(usuario, id, importe)` → suma a ahorrado, si llega/supera importe_objetivo marca `completado_at = now`
- `delete(usuario, id)`
- `ObjetivoNotFoundException`

### F3.5 — TareaService
- `findAll(usuario)` con sort sensato
- `get`, `create(usuario, titulo, descripcion, fechaVencimiento)`, `update`
- `completar(usuario, id)`, `descompletar(usuario, id)`
- `delete`
- `TareaNotFoundException`

### F3.6 — ObjetivoApiController
- `GET /api/objetivos` → lista
- `GET /api/objetivos/{id}` → uno
- `POST /api/objetivos` → 201
- `PUT /api/objetivos/{id}` → 200
- `POST /api/objetivos/{id}/aportar` body `{importe}` → 200
- `DELETE /api/objetivos/{id}` → 204

### F3.7 — TareaApiController
- `GET /api/tareas` → lista
- `POST /api/tareas` → 201
- `PUT /api/tareas/{id}` → 200
- `POST /api/tareas/{id}/completar` → 200
- `POST /api/tareas/{id}/descompletar` → 200
- `DELETE /api/tareas/{id}` → 204

### F3.8 — ObjetivoWebController + plantillas
- `/objetivos` lista con tarjetas, barra de progreso por objetivo
- `/objetivos/nuevo`, `/objetivos/{id}/editar`
- `/objetivos/{id}` detalle con form de aportar
- `/objetivos/{id}/borrar`

### F3.9 — TareaWebController + plantilla
- `/tareas` lista con checkboxes para completar/descompletar
- `/tareas/nuevo` y inline edit
- `/tareas/{id}/borrar`

### F3.10 — Tests integración
- ObjetivoServiceTest: crear, aportar parcial, aportar y completar, no acceso cross-user
- TareaServiceTest: crear, completar, descompletar
- ObjetivoApiControllerTest: register + crear + listar + aportar + 401 sin auth

### F3.11 — Actualizar nav + index
Añadir links a /objetivos y /tareas en la home autenticada y en el nav de movimientos/categorías.

### F3.12 — Verify + push + redeploy + README
- Probar local con curl
- Push a main
- Esperar CI verde y Render redeploy
- Verificar en producción
- Actualizar README con endpoints y marcar Fase 3 ✅ en el roadmap

## Exit criteria

- 12 tasks committed
- `./mvnw verify` verde (≥ 19 tests)
- CI verde
- Demo Render con /objetivos y /tareas funcionando
- README actualizado
