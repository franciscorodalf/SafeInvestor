# SafeInvestor

> Aplicación web de finanzas personales — gestiona gastos, ahorros, objetivos y tareas financieras, y aprende tips de economía.

**Estado:** v2 scaffold (Fase 0 completada). Auth y features en desarrollo.

## Stack

- **Java 21**, **Spring Boot 3.3** (Web, Data JPA, Security, Thymeleaf, Validation, Actuator)
- **PostgreSQL 16** + migraciones con **Flyway**
- **JUnit 5**, **Mockito**, **Testcontainers** (integración en Fase 2)
- **Maven**, **Docker**, **GitHub Actions**

## Ejecutar en local

Requisitos: Java 21+, Docker Desktop.

```bash
docker compose up -d
./mvnw spring-boot:run
```

- App: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`

## Tests

```bash
docker compose up -d   # Postgres debe estar levantado
./mvnw test
```

En CI, GitHub Actions levanta el mismo Postgres como service container.

## v1 (JavaFX desktop)

La versión original desarrollada durante 1ºDAM se conserva en:

- Tag: [`v1-javafx`](https://github.com/franciscorodalf/SafeInvestor/tree/v1-javafx)
- Rama: [`legacy/javafx`](https://github.com/franciscorodalf/SafeInvestor/tree/legacy/javafx)

## Licencia

Ver [LICENSE](LICENSE).
