# SafeInvestor

> Aplicación web de finanzas personales — gestiona gastos, ahorros, objetivos y tareas financieras, y aprende tips de economía.

[![CI](https://github.com/franciscorodalf/SafeInvestor/actions/workflows/ci.yml/badge.svg)](https://github.com/franciscorodalf/SafeInvestor/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=spring)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

**Estado:** v2 Fase 8 desplegada. Auth + movimientos + categorías + objetivos + tareas + dashboard con gráficas Chart.js + export CSV + informe imprimible + tips de economía + i18n ES/EN + dark mode + **emails (reset, objetivo cumplido, recordatorios diarios) + importación CSV bancario con auto-detección**.

📐 **Memoria técnica**: [ARQUITECTURA.md](ARQUITECTURA.md) — arquitectura por capas, modelo ER, decisiones técnicas, despliegue, seguridad.

📚 **API docs**: [Swagger UI](https://safeinvestor.onrender.com/swagger-ui.html) — UI interactivo. Login en `/api/auth/login`, pega el `token` en **Authorize**, llama a cualquier endpoint.

## 🚀 Demo en vivo

👉 **[https://safeinvestor.onrender.com](https://safeinvestor.onrender.com)**

Regístrate en `/register` y prueba `/login`. La API REST está en `/api/auth/*`.

> ⚠️ La demo corre en el plan free de Render, así que la app se duerme tras 15 min sin tráfico. El primer hit puede tardar ~30-60s en despertar. Pasado eso, va fluida.

### Probar la API rápido

```bash
# Registro
curl -X POST https://safeinvestor.onrender.com/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"tu@email.com","nombre":"Tu Nombre","password":"password123"}'

# Login
curl -X POST https://safeinvestor.onrender.com/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"tu@email.com","password":"password123"}'
```

## Stack

- **Java 21**, **Spring Boot 3.3** (Web, Data JPA, Security, Thymeleaf, Validation, Actuator)
- **PostgreSQL 16** + migraciones con **Flyway**
- **Spring Security**: doble filter chain — sesión cookie para web + JWT (jjwt 0.12) para API
- **BCrypt** para hashing de contraseñas
- **JUnit 5** + **Mockito** + **MockMvc** (19 tests de integración)
- **Maven**, **Docker** (imagen multi-stage), **GitHub Actions** (CI verde)
- **Render** para el deploy público

## Ejecutar en local

Requisitos: Java 21+, Docker Desktop.

```bash
docker compose up -d
./mvnw spring-boot:run
```

- App: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- Registro: `http://localhost:8080/register`
- Login: `http://localhost:8080/login`

## Tests

```bash
docker compose up -d   # Postgres debe estar levantado
./mvnw test
```

En CI, GitHub Actions levanta el mismo Postgres como service container.

## Endpoints

### Web (Thymeleaf + sesión)

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/` | Landing |
| GET/POST | `/login` | Login form |
| GET/POST | `/register` | Registro |
| GET/POST | `/forgot` | Solicitar reset de contraseña (link en logs en dev) |
| GET/POST | `/reset/{token}` | Establecer nueva contraseña |
| POST | `/logout` | Cerrar sesión |
| GET | `/movimientos` | Lista paginada con filtros (categoría, fechas) |
| GET/POST | `/movimientos/nuevo` | Nuevo gasto/ingreso |
| GET/POST | `/movimientos/{id}/editar` | Editar |
| POST | `/movimientos/{id}/borrar` | Eliminar |
| GET/POST | `/categorias` | Gestión de categorías |
| GET | `/movimientos/export.csv` | Descarga CSV de movimientos |
| GET | `/movimientos/informe` | Informe imprimible → PDF desde navegador |
| GET/POST | `/movimientos/import` | Subir CSV bancario → preview editable → confirmar |
| POST | `/movimientos/import/confirm` | Guardar las filas seleccionadas tras revisar |
| GET | `/estadisticas/gastos-por-categoria` | JSON gastos del mes por categoría |
| GET | `/estadisticas/evolucion-mensual` | JSON ingresos vs gastos últimos 6 meses |
| GET | `/objetivos` | Lista con barra de progreso |
| GET/POST | `/objetivos/nuevo` | Crear objetivo |
| GET | `/objetivos/{id}` | Detalle + form de aportar |
| POST | `/objetivos/{id}/aportar` | Sumar al ahorrado |
| GET/POST | `/tareas` | Lista + crear inline |
| POST | `/tareas/{id}/completar` | Marcar completada |
| POST | `/tareas/{id}/descompletar` | Desmarcar |
| GET | `/tips` | Listado de tips de economía con filtro por categoría (modal de detalle) |

Cualquier ruta acepta `?lang=es` o `?lang=en` para cambiar el idioma (persiste en cookie).
Toggle dark/light arriba a la derecha (persiste en localStorage).

### API REST (JWT)

| Método | Ruta | Códigos |
|--------|------|---------|
| POST | `/api/auth/register` | 201 / 409 |
| POST | `/api/auth/login` | 200 / 401 |
| GET | `/api/categorias` | 200 |
| POST | `/api/categorias` | 201 / 409 |
| PUT | `/api/categorias/{id}` | 200 / 404 |
| DELETE | `/api/categorias/{id}` | 204 / 404 |
| GET | `/api/movimientos?categoriaId=&desde=&hasta=&page=` | 200 |
| POST | `/api/movimientos` | 201 |
| GET | `/api/movimientos/{id}` | 200 / 404 |
| PUT | `/api/movimientos/{id}` | 200 / 404 |
| DELETE | `/api/movimientos/{id}` | 204 / 404 |
| GET | `/api/objetivos` | 200 |
| POST | `/api/objetivos` | 201 |
| GET | `/api/objetivos/{id}` | 200 / 404 |
| PUT | `/api/objetivos/{id}` | 200 / 404 |
| POST | `/api/objetivos/{id}/aportar` | 200 (autocompleta si alcanza objetivo) |
| DELETE | `/api/objetivos/{id}` | 204 |
| GET | `/api/tareas` | 200 |
| POST | `/api/tareas` | 201 |
| PUT | `/api/tareas/{id}` | 200 / 404 |
| POST | `/api/tareas/{id}/completar` | 200 |
| POST | `/api/tareas/{id}/descompletar` | 200 |
| DELETE | `/api/tareas/{id}` | 204 |

Todos los endpoints de `/api/` (excepto `/api/auth/*`) requieren `Authorization: Bearer <JWT>`. Sin auth → 401.

### Actuator

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/actuator/health` | Healthcheck (200 UP) |

## v1 (JavaFX desktop)

La versión original desarrollada durante 1ºDAM se conserva en:

- Tag: [`v1-javafx`](https://github.com/franciscorodalf/SafeInvestor/tree/v1-javafx)
- Rama: [`legacy/javafx`](https://github.com/franciscorodalf/SafeInvestor/tree/legacy/javafx)

## Roadmap

- [x] **Fase 0** — Scaffold Spring Boot + Postgres + CI
- [x] **Fase 1** — Auth (sesión web + JWT API) + reset de contraseña + deploy en Render
- [x] **Fase 2** — CRUD de movimientos (gastos/ingresos) + categorías con seed por defecto
- [x] **Fase 3** — Objetivos de ahorro (con aportes + autocompletado) + tareas financieras (con vencimientos)
- [x] **Fase 4** — Dashboard con gráficas Chart.js (donut por categoría + line evolución 6 meses) + export CSV (formato Excel) + informe imprimible (HTML → PDF desde navegador)
- [x] **Fase 5** — Tips de economía (30 consejos, modal de detalle) + i18n ES/EN (cookie + selector en nav) + dark mode (toggle sol/luna persistente, sin FOUC)
- [x] **Fase 7** — Notificaciones por email (Spring Mail + plantillas Thymeleaf): reset de contraseña real, objetivo cumplido y job @Scheduled diario de tareas vencidas. Fallback a log si no hay SMTP configurado.
- [x] **Fase 8** — Importar CSV bancario con auto-detección (separador, decimal, fecha), preview editable y auto-sugerencia de categoría por keywords.

## Licencia

Ver [LICENSE](LICENSE).
