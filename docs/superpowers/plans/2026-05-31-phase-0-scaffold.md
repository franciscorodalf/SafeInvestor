# SafeInvestor v2 — Phase 0: Scaffold Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wipe the JavaFX v1 from `main`, scaffold a Spring Boot 3.3 web application with Postgres in Docker, Flyway migrations, a smoke test, and a green GitHub Actions CI pipeline. End state: `mvn spring-boot:run` boots an empty app, `mvn test` passes, CI is green on `main`.

**Architecture:** Single Maven module Spring Boot 3 app, Java 21. PostgreSQL 16 via `docker-compose` for dev/test. Flyway manages schema. Project structure follows standard Spring layout (`controller`, `service`, `repository`, `entity`, `dto`, `config`). No business code yet — only the skeleton, configuration, and proof that everything wires together.

**Tech Stack:** Java 21, Spring Boot 3.3.x, Spring Web, Spring Data JPA, Spring Security (placeholder config), Thymeleaf, Flyway, PostgreSQL 16, Lombok, Bean Validation, Spring Boot Actuator, Spring Boot DevTools, Testcontainers (Postgres), JUnit 5, Maven, Docker, GitHub Actions.

**Prerequisites confirmed before starting:**
- Backup tag `v1-javafx` and branch `legacy/javafx` already pushed to `origin`. The JavaFX project is recoverable from those refs.
- Working tree clean (`git status` empty).
- Local toolchain: Java 21, Maven 3.9+, Docker Desktop running.

---

## File Structure

After Phase 0 the repo looks like this (only files this plan creates/modifies are listed):

```
.
├── .github/
│   └── workflows/
│       └── ci.yml                              # GitHub Actions: build + test on push/PR
├── .gitignore                                  # Java/Maven/IDE ignores
├── .mvn/                                       # Maven wrapper (generated)
├── README.md                                   # Replaced with v2 stub
├── compose.yaml                                # Postgres 16 dev container
├── docs/
│   └── superpowers/
│       └── plans/
│           └── 2026-05-31-phase-0-scaffold.md  # THIS plan
├── mvnw, mvnw.cmd                              # Maven wrapper scripts
├── pom.xml                                     # NEW: Spring Boot pom
└── src/
    ├── main/
    │   ├── java/
    │   │   └── es/franciscorodalf/safeinvestor/
    │   │       ├── SafeInvestorApplication.java          # @SpringBootApplication entrypoint
    │   │       └── config/
    │   │           └── SecurityConfig.java               # Permit-all stub; tightened in Phase 1
    │   └── resources/
    │       ├── application.yml                            # Default profile config
    │       ├── application-test.yml                       # Test profile config (Testcontainers)
    │       ├── db/migration/
    │       │   └── V1__init.sql                           # Empty placeholder migration
    │       ├── static/.gitkeep
    │       └── templates/
    │           └── index.html                             # Trivial Thymeleaf landing page
    └── test/
        └── java/
            └── es/franciscorodalf/safeinvestor/
                ├── SafeInvestorApplicationTests.java     # Context-loads smoke test
                └── support/
                    └── PostgresTestContainer.java        # Reusable @Container helper
```

**Files deleted in Task 1:** every file currently in the repo except `LICENSE`, `.git/`, and the new `docs/superpowers/plans/` tree (which contains THIS plan and must survive).

---

## Task 1: Wipe v1 contents from `main`

**Files:**
- Delete: every tracked file in repo EXCEPT `LICENSE` and `docs/superpowers/plans/2026-05-31-phase-0-scaffold.md`.
- Keep untouched: `.git/` (history stays, backup refs already on origin).

- [ ] **Step 1: Verify backup refs exist on origin**

Run:
```bash
git ls-remote --tags origin v1-javafx
git ls-remote --heads origin legacy/javafx
```
Expected: each command prints one line with a SHA. If either is empty, STOP and push the backup first (`git push origin v1-javafx legacy/javafx`).

- [ ] **Step 2: Verify working tree is clean**

Run:
```bash
git status --porcelain
```
Expected: empty output. If not empty, commit or stash before continuing.

- [ ] **Step 3: List what will be deleted**

Run:
```bash
git ls-files | grep -vE '^(LICENSE|docs/superpowers/plans/)' | sort
```
Expected: a list of every JavaFX/Maven file currently in the repo. Eyeball it to confirm nothing surprising is in there (e.g. uncommitted secrets you forgot about).

- [ ] **Step 4: Delete the listed files**

Run:
```bash
git ls-files | grep -vE '^(LICENSE|docs/superpowers/plans/)' | xargs git rm -r
```
Expected: many lines of `rm 'path/to/file'`. No errors.

- [ ] **Step 5: Remove now-empty directories**

Run:
```bash
find . -type d -empty -not -path './.git*' -delete
```
Expected: silent success. `ls` should now show only `LICENSE`, `docs/`, and `.git/`.

- [ ] **Step 6: Commit the wipe**

Run:
```bash
git add -A
git commit -m "chore: wipe v1 JavaFX from main (preserved in tag v1-javafx and branch legacy/javafx)"
```
Expected: commit succeeds. `git log --oneline -1` shows the new commit.

---

## Task 2: Create `.gitignore`

**Files:**
- Create: `.gitignore`

- [ ] **Step 1: Write `.gitignore`**

Create `.gitignore` with this exact content:
```gitignore
# Maven
target/
!.mvn/wrapper/maven-wrapper.jar
!**/src/main/**/target/
!**/src/test/**/target/

# IDEs
.idea/
*.iml
*.ipr
*.iws
.vscode/
.project
.classpath
.settings/
.factorypath
nbproject/private/
nbbuild/
nbdist/
.nb-gradle/

# OS
.DS_Store
Thumbs.db

# Spring Boot
HELP.md

# Logs
*.log
logs/

# Env / secrets
.env
.env.local
*.pem

# Docker volumes
postgres-data/
```

- [ ] **Step 2: Commit**

Run:
```bash
git add .gitignore
git commit -m "chore: add gitignore for Java/Maven/IDE/Docker"
```

---

## Task 3: Create `pom.xml` for Spring Boot

**Files:**
- Create: `pom.xml`

- [ ] **Step 1: Write `pom.xml`**

Create `pom.xml` with this exact content:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>

    <groupId>es.franciscorodalf</groupId>
    <artifactId>safeinvestor</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>safeinvestor</name>
    <description>Personal finance web app — SafeInvestor v2</description>

    <properties>
        <java.version>21</java.version>
        <testcontainers.version>1.20.2</testcontainers.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>testcontainers-bom</artifactId>
                <version>${testcontainers.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Generate Maven wrapper**

Run:
```bash
mvn -N wrapper:wrapper -Dmaven=3.9.9
```
Expected: creates `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`. Exit code 0.

- [ ] **Step 3: Verify Maven resolves dependencies**

Run:
```bash
./mvnw -q dependency:resolve
```
Expected: completes without errors. First run downloads many artifacts; subsequent runs are quick.

- [ ] **Step 4: Commit**

Run:
```bash
git add pom.xml mvnw mvnw.cmd .mvn
git commit -m "build: scaffold Spring Boot 3.3 pom + Maven wrapper"
```

---

## Task 4: Create `compose.yaml` for local Postgres

**Files:**
- Create: `compose.yaml`

- [ ] **Step 1: Write `compose.yaml`**

Create `compose.yaml` with this exact content:
```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: safeinvestor-postgres
    environment:
      POSTGRES_DB: safeinvestor
      POSTGRES_USER: safeinvestor
      POSTGRES_PASSWORD: safeinvestor
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U safeinvestor -d safeinvestor"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres-data:
```

- [ ] **Step 2: Start Postgres and verify health**

Run:
```bash
docker compose up -d
docker compose ps
```
Expected: `safeinvestor-postgres` is listed with status `running (healthy)` after ~10s. If `(starting)`, wait and re-run `docker compose ps`.

- [ ] **Step 3: Verify connection from host**

Run:
```bash
docker exec safeinvestor-postgres psql -U safeinvestor -d safeinvestor -c '\l'
```
Expected: table listing includes `safeinvestor` database. Exit code 0.

- [ ] **Step 4: Commit**

Run:
```bash
git add compose.yaml
git commit -m "build: add Postgres 16 dev compose service"
```

---

## Task 5: Create application configuration

**Files:**
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-test.yml`

- [ ] **Step 1: Write `src/main/resources/application.yml`**

Create with this exact content:
```yaml
spring:
  application:
    name: safeinvestor

  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/safeinvestor}
    username: ${DB_USERNAME:safeinvestor}
    password: ${DB_PASSWORD:safeinvestor}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          time_zone: UTC
        format_sql: true
    open-in-view: false

  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

  thymeleaf:
    cache: false

server:
  port: ${PORT:8080}
  error:
    include-message: always

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized

logging:
  level:
    es.franciscorodalf.safeinvestor: INFO
    org.springframework.security: INFO
```

- [ ] **Step 2: Write `src/main/resources/application-test.yml`**

Create with this exact content:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    clean-disabled: false
  thymeleaf:
    cache: false

logging:
  level:
    org.testcontainers: INFO
    com.github.dockerjava: WARN
```

- [ ] **Step 3: Commit**

Run:
```bash
git add src/main/resources/application.yml src/main/resources/application-test.yml
git commit -m "feat: add application config for dev and test profiles"
```

---

## Task 6: Create the Spring Boot entrypoint

**Files:**
- Create: `src/main/java/es/franciscorodalf/safeinvestor/SafeInvestorApplication.java`

- [ ] **Step 1: Write the entrypoint**

Create `src/main/java/es/franciscorodalf/safeinvestor/SafeInvestorApplication.java`:
```java
package es.franciscorodalf.safeinvestor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SafeInvestorApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafeInvestorApplication.class, args);
    }
}
```

- [ ] **Step 2: Verify the project compiles**

Run:
```bash
./mvnw -q compile
```
Expected: BUILD SUCCESS. No errors.

- [ ] **Step 3: Commit**

Run:
```bash
git add src/main/java/es/franciscorodalf/safeinvestor/SafeInvestorApplication.java
git commit -m "feat: add Spring Boot application entrypoint"
```

---

## Task 7: Add permit-all security stub

**Files:**
- Create: `src/main/java/es/franciscorodalf/safeinvestor/config/SecurityConfig.java`

This stub disables CSRF and permits all requests so the app boots without forcing a login screen. Phase 1 (Auth) replaces this with real security.

- [ ] **Step 1: Write `SecurityConfig.java`**

Create `src/main/java/es/franciscorodalf/safeinvestor/config/SecurityConfig.java`:
```java
package es.franciscorodalf.safeinvestor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
```

- [ ] **Step 2: Verify compile**

Run:
```bash
./mvnw -q compile
```
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

Run:
```bash
git add src/main/java/es/franciscorodalf/safeinvestor/config/SecurityConfig.java
git commit -m "feat: add permit-all security stub (replaced in Phase 1)"
```

---

## Task 8: Add empty Flyway migration and Thymeleaf landing page

**Files:**
- Create: `src/main/resources/db/migration/V1__init.sql`
- Create: `src/main/resources/static/.gitkeep`
- Create: `src/main/resources/templates/index.html`

- [ ] **Step 1: Write `V1__init.sql`**

Create `src/main/resources/db/migration/V1__init.sql`:
```sql
-- Phase 0 placeholder migration.
-- Real entities are introduced in Phase 2 (data model).
-- This file exists so Flyway has at least one migration to apply on first boot.
SELECT 1;
```

- [ ] **Step 2: Create `static/.gitkeep`**

Create an empty file at `src/main/resources/static/.gitkeep` (no content). This reserves the directory in git for future CSS/JS assets.

- [ ] **Step 3: Write `index.html`**

Create `src/main/resources/templates/index.html`:
```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>SafeInvestor</title>
</head>
<body>
    <h1>SafeInvestor v2</h1>
    <p>Scaffold ready. Auth and features ship in subsequent phases.</p>
</body>
</html>
```

- [ ] **Step 4: Commit**

Run:
```bash
git add src/main/resources/db/migration/V1__init.sql src/main/resources/static/.gitkeep src/main/resources/templates/index.html
git commit -m "feat: add placeholder Flyway migration and landing template"
```

---

## Task 9: Write the failing smoke test

**Files:**
- Create: `src/test/java/es/franciscorodalf/safeinvestor/support/PostgresTestContainer.java`
- Create: `src/test/java/es/franciscorodalf/safeinvestor/SafeInvestorApplicationTests.java`

- [ ] **Step 1: Write the Testcontainers helper**

Create `src/test/java/es/franciscorodalf/safeinvestor/support/PostgresTestContainer.java`:
```java
package es.franciscorodalf.safeinvestor.support;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresTestContainer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("safeinvestor")
            .withUsername("safeinvestor")
            .withPassword("safeinvestor")
            .withReuse(true);

    static {
        POSTGRES.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        TestPropertyValues.of(
            "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
            "spring.datasource.username=" + POSTGRES.getUsername(),
            "spring.datasource.password=" + POSTGRES.getPassword()
        ).applyTo(ctx.getEnvironment());
    }
}
```

- [ ] **Step 2: Write the smoke test**

Create `src/test/java/es/franciscorodalf/safeinvestor/SafeInvestorApplicationTests.java`:
```java
package es.franciscorodalf.safeinvestor;

import es.franciscorodalf.safeinvestor.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
class SafeInvestorApplicationTests {

    @Test
    void contextLoads() {
        // Asserts Spring context boots cleanly against a real Postgres.
    }
}
```

- [ ] **Step 3: Run the test and confirm it passes**

Run:
```bash
./mvnw test
```
Expected: `BUILD SUCCESS`, `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`. On the first run Testcontainers pulls `postgres:16-alpine` (~30s). If Docker is not running, the test fails with a `DockerClientException` — start Docker Desktop and retry.

- [ ] **Step 4: Commit**

Run:
```bash
git add src/test/java/es/franciscorodalf/safeinvestor/support/PostgresTestContainer.java src/test/java/es/franciscorodalf/safeinvestor/SafeInvestorApplicationTests.java
git commit -m "test: add context-loads smoke test backed by Testcontainers Postgres"
```

---

## Task 10: Verify the app boots end-to-end manually

**Files:** none modified — this is a manual verification step.

- [ ] **Step 1: Ensure dev Postgres is up**

Run:
```bash
docker compose ps
```
Expected: `safeinvestor-postgres` is `running (healthy)`. If not, run `docker compose up -d` and wait.

- [ ] **Step 2: Boot the app**

Run:
```bash
./mvnw spring-boot:run
```
Expected logs include:
- `Flyway Community Edition ... by Redgate`
- `Successfully validated 1 migration` (the empty V1)
- `Tomcat started on port 8080`
- `Started SafeInvestorApplication in N.NNN seconds`

- [ ] **Step 3: Hit the landing page**

In another terminal, run:
```bash
curl -s http://localhost:8080/ | head -5
```
Expected: HTML containing `<h1>SafeInvestor v2</h1>`.

- [ ] **Step 4: Hit the actuator health endpoint**

Run:
```bash
curl -s http://localhost:8080/actuator/health
```
Expected: JSON `{"status":"UP"}`.

- [ ] **Step 5: Stop the app**

Press `Ctrl+C` in the terminal running `spring-boot:run`. Expected: graceful shutdown logs, exit code 0.

- [ ] **Step 6: No commit needed**

This task verifies behavior. Nothing to commit.

---

## Task 11: Add GitHub Actions CI

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Write `ci.yml`**

Create `.github/workflows/ci.yml`:
```yaml
name: CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  build:
    name: Build & Test
    runs-on: ubuntu-latest
    timeout-minutes: 15

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Verify Maven wrapper
        run: ./mvnw -v

      - name: Build and test
        run: ./mvnw -B verify
```

- [ ] **Step 2: Commit and push**

Run:
```bash
git add .github/workflows/ci.yml
git commit -m "ci: add GitHub Actions build + test workflow"
git push origin main
```

- [ ] **Step 3: Verify CI is green**

Open `https://github.com/franciscorodalf/SafeInvestor/actions` in a browser. Expected: the latest run of "CI" finishes with a green check within ~5 minutes. If red, open the failed step's logs, fix the root cause locally, push again. Do not merge red.

---

## Task 12: Replace the README with a v2 stub

**Files:**
- Create: `README.md`

The full v2 README (screenshots, demo link, badges) lands in the final polish phase. This stub explains the current state and points to the legacy refs.

- [ ] **Step 1: Write the new README**

Create `README.md` with this exact content:
````markdown
# SafeInvestor

> Personal finance web app — track expenses, savings goals, financial tasks, and learn money tips.

**Status:** v2 scaffold (Phase 0 complete). Auth and feature work in progress.

## Stack

- Java 21, Spring Boot 3.3 (Web, Data JPA, Security, Thymeleaf, Validation, Actuator)
- PostgreSQL 16 + Flyway migrations
- JUnit 5, Mockito, Testcontainers
- Maven, Docker, GitHub Actions

## Run locally

Prerequisites: Java 21, Docker Desktop.

```bash
docker compose up -d
./mvnw spring-boot:run
```

App at `http://localhost:8080`. Health at `http://localhost:8080/actuator/health`.

## Tests

```bash
./mvnw test
```

Testcontainers spins up a disposable Postgres for the integration tests.

## v1 (JavaFX desktop)

The original desktop version built during 1ºDAM is preserved at:

- Tag: [`v1-javafx`](https://github.com/franciscorodalf/SafeInvestor/tree/v1-javafx)
- Branch: [`legacy/javafx`](https://github.com/franciscorodalf/SafeInvestor/tree/legacy/javafx)

## License

See [LICENSE](LICENSE).
````

- [ ] **Step 2: Commit and push**

Run:
```bash
git add README.md
git commit -m "docs: replace README with v2 stub"
git push origin main
```

- [ ] **Step 3: Confirm CI still green after README push**

Visit the Actions tab. Expected: the new commit's CI run finishes green.

---

## Phase 0 done — exit criteria

All of the following must be true before declaring Phase 0 complete and moving to Phase 1 (Auth):

- [ ] `git log --oneline` shows the wipe, scaffold, config, and CI commits in order.
- [ ] `./mvnw verify` is green locally.
- [ ] `docker compose up -d && ./mvnw spring-boot:run` boots the app; `curl localhost:8080/` returns the landing HTML; `curl localhost:8080/actuator/health` returns `{"status":"UP"}`.
- [ ] GitHub Actions CI is green on the latest `main` commit.
- [ ] Tag `v1-javafx` and branch `legacy/javafx` are visible on GitHub under the repo's Tags and Branches.
- [ ] `README.md` reflects the v2 stack and links to the legacy refs.

When all six are checked, Phase 0 is done. Phase 1 (Spring Security + JWT + login/register/recover + Railway deploy) gets its own plan written with the real scaffold in hand.
