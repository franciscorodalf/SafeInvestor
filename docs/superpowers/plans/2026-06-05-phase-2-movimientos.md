# SafeInvestor v2 — Phase 2: Movimientos + Categorías Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Add gastos/ingresos tracking. Each user gets a seeded set of default categorías on register. CRUD para movimientos y categorías, vía web (Thymeleaf) y REST (`/api/movimientos`, `/api/categorias`). Listado con paginación + filtro por categoría y rango de fechas. Despliegue actualizado en Render.

**Architecture:** Two new entities (`Categoria`, `Movimiento`) scoped por `usuario_id`. Every read query filters by the authenticated user (extracted from `SecurityContextHolder`). Importes como `NUMERIC(12,2)` (BigDecimal en Java) para evitar errores de coma flotante. `TipoMovimiento` enum (GASTO, INGRESO). Categorías default seedeadas en `UsuarioService.register()` cuando se crea un usuario nuevo. Web flujo: lista paginada + formulario. REST flujo: CRUD JSON.

**Tech Stack additions:** Spring Data JPA pagination (ya en pom), nada nuevo. Tailwind sigue por CDN.

**Prerequisites:**
- Fase 1 cerrada, demo viva en https://safeinvestor.onrender.com.
- 8 tests pasando local + CI verde.
- Docker, Postgres dev container corriendo.

**Scope choices confirmed before writing:**
- Categorías: **seed default al registrarse** (8 gastos típicos + 3 ingresos).
- UI: **Web (Thymeleaf) + REST API** (mismo doble patrón que Fase 1).
- Importes: BigDecimal con `NUMERIC(12,2)` (hasta 9 999 999 999,99 €).
- Fechas: `LocalDate` (solo día, sin hora).
- Pagination: 20 por página, ordenadas por fecha desc.
- Ownership: cada movimiento/categoría pertenece a 1 usuario. Filter en cada query.

**Out of scope:** recurrencias mensuales, attachments (recibos), import desde CSV, multiusuario/familia.

---

## File Structure

```
src/main/java/es/franciscorodalf/safeinvestor/
├── auth/service/UsuarioService.java                # MODIFICADO: seed categorías default tras register
├── movimientos/
│   ├── api/
│   │   ├── CategoriaApiController.java
│   │   ├── MovimientoApiController.java
│   │   └── dto/
│   │       ├── CategoriaRequest.java
│   │       ├── CategoriaResponse.java
│   │       ├── MovimientoRequest.java
│   │       └── MovimientoResponse.java
│   ├── web/
│   │   ├── CategoriaWebController.java
│   │   ├── MovimientoWebController.java
│   │   └── form/
│   │       ├── CategoriaForm.java
│   │       └── MovimientoForm.java
│   ├── domain/
│   │   ├── TipoMovimiento.java
│   │   ├── Categoria.java
│   │   ├── Movimiento.java
│   │   ├── CategoriaRepository.java
│   │   └── MovimientoRepository.java
│   ├── service/
│   │   ├── CategoriaService.java                   # incluye default seed
│   │   └── MovimientoService.java
│   └── security/CurrentUser.java                   # helper para resolver Usuario desde SecurityContext

src/main/resources/
├── db/migration/V3__movimientos_categorias.sql
└── templates/
    ├── layout.html                                 # MODIFICADO: añadir links nav
    ├── movimientos/list.html
    ├── movimientos/form.html
    └── categorias/list.html

src/test/java/.../movimientos/
├── service/CategoriaServiceTest.java
├── service/MovimientoServiceTest.java
└── api/MovimientoApiControllerTest.java
```

---

## Task 1 (F2.1): Migración V3 — categorías + movimientos

**File:** `src/main/resources/db/migration/V3__movimientos_categorias.sql`

- [ ] Crear el archivo con este contenido EXACTO:

```sql
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
```

- [ ] Aplicar y verificar:

```bash
docker compose down -v && docker compose up -d
until docker exec safeinvestor-postgres pg_isready -U safeinvestor -d safeinvestor; do sleep 1; done
./mvnw test
```

Expected: BUILD SUCCESS, 8 tests pasan (smoke + auth). Flyway aplica V1, V2 y V3 limpias.

- [ ] Commit:

```bash
git add src/main/resources/db/migration/V3__movimientos_categorias.sql
git commit -m "feat: migración Flyway V3 con tablas categorias y movimientos"
```

---

## Task 2 (F2.2): `TipoMovimiento` enum + `Categoria` entity + repositorio

**Files:**
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/TipoMovimiento.java`
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/Categoria.java`
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/CategoriaRepository.java`

- [ ] `TipoMovimiento.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.domain;

public enum TipoMovimiento {
    GASTO, INGRESO
}
```

- [ ] `Categoria.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "categorias",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "nombre"}))
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 7)
    private String color = "#6B7280";

    @Column(length = 50)
    private String icono;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    public Categoria() {}

    public Categoria(Usuario usuario, String nombre, String color, String icono) {
        this.usuario = usuario;
        this.nombre = nombre;
        if (color != null) this.color = color;
        this.icono = icono;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getNombre() { return nombre; }
    public String getColor() { return color; }
    public String getIcono() { return icono; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setColor(String color) { this.color = color; }
    public void setIcono(String icono) { this.icono = icono; }
}
```

- [ ] `CategoriaRepository.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioOrderByNombre(Usuario usuario);
    Optional<Categoria> findByIdAndUsuario(Long id, Usuario usuario);
    boolean existsByUsuarioAndNombreIgnoreCase(Usuario usuario, String nombre);
}
```

- [ ] `./mvnw -q compile` → SUCCESS
- [ ] Commit:

```bash
git add src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/TipoMovimiento.java src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/Categoria.java src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/CategoriaRepository.java
git commit -m "feat: entidad Categoria, TipoMovimiento enum y repositorio JPA"
```

---

## Task 3 (F2.3): `Movimiento` entity + repositorio

**Files:**
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/Movimiento.java`
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/MovimientoRepository.java`

- [ ] `Movimiento.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    public Movimiento() {}

    public Movimiento(Usuario usuario, Categoria categoria, TipoMovimiento tipo,
                      BigDecimal importe, String descripcion, LocalDate fecha) {
        this.usuario = usuario;
        this.categoria = categoria;
        this.tipo = tipo;
        this.importe = importe;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Categoria getCategoria() { return categoria; }
    public TipoMovimiento getTipo() { return tipo; }
    public BigDecimal getImporte() { return importe; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setCategoria(Categoria c) { this.categoria = c; }
    public void setTipo(TipoMovimiento t) { this.tipo = t; }
    public void setImporte(BigDecimal i) { this.importe = i; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public void setFecha(LocalDate f) { this.fecha = f; }
}
```

- [ ] `MovimientoRepository.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    Optional<Movimiento> findByIdAndUsuario(Long id, Usuario usuario);

    @Query("""
        SELECT m FROM Movimiento m
        WHERE m.usuario = :usuario
          AND (:categoriaId IS NULL OR m.categoria.id = :categoriaId)
          AND (:desde IS NULL OR m.fecha >= :desde)
          AND (:hasta IS NULL OR m.fecha <= :hasta)
        ORDER BY m.fecha DESC, m.id DESC
        """)
    Page<Movimiento> search(@Param("usuario") Usuario usuario,
                            @Param("categoriaId") Long categoriaId,
                            @Param("desde") LocalDate desde,
                            @Param("hasta") LocalDate hasta,
                            Pageable pageable);
}
```

- [ ] `./mvnw -q compile` → SUCCESS
- [ ] Commit:

```bash
git add src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/Movimiento.java src/main/java/es/franciscorodalf/safeinvestor/movimientos/domain/MovimientoRepository.java
git commit -m "feat: entidad Movimiento con query de búsqueda paginada y filtros"
```

---

## Task 4 (F2.4): `CurrentUser` helper

**File:** `src/main/java/es/franciscorodalf/safeinvestor/movimientos/security/CurrentUser.java`

Pequeño helper para extraer el `Usuario` actual desde `SecurityContextHolder`. Se inyectará en los controllers y servicios.

- [ ] Crear:

```java
package es.franciscorodalf.safeinvestor.movimientos.security;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.domain.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UsuarioRepository usuarios;

    public CurrentUser(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        String email = auth.getName();
        return usuarios.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado no existe: " + email));
    }
}
```

- [ ] `./mvnw -q compile` → SUCCESS
- [ ] Commit:

```bash
git add src/main/java/es/franciscorodalf/safeinvestor/movimientos/security/CurrentUser.java
git commit -m "feat: helper CurrentUser para resolver Usuario desde SecurityContext"
```

---

## Task 5 (F2.5): `CategoriaService` con defaults

**File:** `src/main/java/es/franciscorodalf/safeinvestor/movimientos/service/CategoriaService.java`

- [ ] Crear:

```java
package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import es.franciscorodalf.safeinvestor.movimientos.domain.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    /** Categorías que se crean automáticamente al registrarse un usuario. */
    static final List<DefaultCategoria> DEFAULTS = List.of(
        new DefaultCategoria("Comida",      "#F59E0B"),
        new DefaultCategoria("Transporte",  "#3B82F6"),
        new DefaultCategoria("Hogar",       "#10B981"),
        new DefaultCategoria("Ocio",        "#EC4899"),
        new DefaultCategoria("Salud",       "#EF4444"),
        new DefaultCategoria("Compras",     "#8B5CF6"),
        new DefaultCategoria("Servicios",   "#6B7280"),
        new DefaultCategoria("Otros gastos","#9CA3AF"),
        new DefaultCategoria("Nómina",      "#22C55E"),
        new DefaultCategoria("Freelance",   "#14B8A6"),
        new DefaultCategoria("Otros ingresos", "#A3E635")
    );

    private final CategoriaRepository categorias;

    public CategoriaService(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    @Transactional
    public void seedDefaults(Usuario usuario) {
        for (DefaultCategoria d : DEFAULTS) {
            categorias.save(new Categoria(usuario, d.nombre(), d.color(), null));
        }
    }

    public List<Categoria> findAll(Usuario usuario) {
        return categorias.findByUsuarioOrderByNombre(usuario);
    }

    @Transactional
    public Categoria create(Usuario usuario, String nombre, String color, String icono) {
        if (categorias.existsByUsuarioAndNombreIgnoreCase(usuario, nombre)) {
            throw new CategoriaDuplicadaException(nombre);
        }
        return categorias.save(new Categoria(usuario, nombre, color, icono));
    }

    @Transactional
    public Categoria update(Usuario usuario, Long id, String nombre, String color, String icono) {
        Categoria c = categorias.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new CategoriaNotFoundException(id));
        c.setNombre(nombre);
        c.setColor(color);
        c.setIcono(icono);
        return c;
    }

    @Transactional
    public void delete(Usuario usuario, Long id) {
        Categoria c = categorias.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new CategoriaNotFoundException(id));
        categorias.delete(c);
    }

    private record DefaultCategoria(String nombre, String color) {}

    public static class CategoriaNotFoundException extends RuntimeException {
        public CategoriaNotFoundException(Long id) { super("Categoría no encontrada: " + id); }
    }

    public static class CategoriaDuplicadaException extends RuntimeException {
        public CategoriaDuplicadaException(String nombre) { super("Categoría ya existe: " + nombre); }
    }
}
```

- [ ] `./mvnw -q compile` → SUCCESS
- [ ] Commit:

```bash
git add src/main/java/es/franciscorodalf/safeinvestor/movimientos/service/CategoriaService.java
git commit -m "feat: CategoriaService con CRUD y seed de 11 categorías por defecto"
```

---

## Task 6 (F2.6): Hook seed en `UsuarioService.register()`

**File modificado:** `src/main/java/es/franciscorodalf/safeinvestor/auth/service/UsuarioService.java`

Inyectar `CategoriaService` y llamar `seedDefaults(u)` después de guardar el usuario.

- [ ] Reemplazar el archivo con:

```java
package es.franciscorodalf.safeinvestor.auth.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.domain.UsuarioRepository;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final CategoriaService categoriaService;

    public UsuarioService(UsuarioRepository usuarios,
                          PasswordEncoder passwordEncoder,
                          @Lazy CategoriaService categoriaService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.categoriaService = categoriaService;
    }

    @Transactional
    public Usuario register(String email, String nombre, String rawPassword) {
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        Usuario u = new Usuario(email.toLowerCase(), nombre, passwordEncoder.encode(rawPassword));
        Usuario saved = usuarios.save(u);
        categoriaService.seedDefaults(saved);
        return saved;
    }

    @Transactional
    public void changePassword(Usuario usuario, String newRawPassword) {
        usuario.setPasswordHash(passwordEncoder.encode(newRawPassword));
        usuarios.save(usuario);
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException {
        public EmailAlreadyRegisteredException(String email) {
            super("El email ya está registrado: " + email);
        }
    }
}
```

`@Lazy` evita un ciclo de dependencia hipotético.

- [ ] `./mvnw test` — todos los tests existentes deben seguir pasando (UsuarioServiceTest, AuthApiControllerTest). Las categorías default se crearán en los tests también.
- [ ] Commit:

```bash
git add src/main/java/es/franciscorodalf/safeinvestor/auth/service/UsuarioService.java
git commit -m "feat: seed automático de categorías por defecto al registrar un usuario"
```

---

## Task 7 (F2.7): `MovimientoService`

**File:** `src/main/java/es/franciscorodalf/safeinvestor/movimientos/service/MovimientoService.java`

- [ ] Crear:

```java
package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import es.franciscorodalf.safeinvestor.movimientos.domain.CategoriaRepository;
import es.franciscorodalf.safeinvestor.movimientos.domain.Movimiento;
import es.franciscorodalf.safeinvestor.movimientos.domain.MovimientoRepository;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class MovimientoService {

    private static final int PAGE_SIZE = 20;

    private final MovimientoRepository movimientos;
    private final CategoriaRepository categorias;

    public MovimientoService(MovimientoRepository movimientos, CategoriaRepository categorias) {
        this.movimientos = movimientos;
        this.categorias = categorias;
    }

    public Page<Movimiento> search(Usuario usuario, Long categoriaId,
                                   LocalDate desde, LocalDate hasta, int page) {
        return movimientos.search(usuario, categoriaId, desde, hasta,
            PageRequest.of(Math.max(0, page), PAGE_SIZE));
    }

    public Movimiento get(Usuario usuario, Long id) {
        return movimientos.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new MovimientoNotFoundException(id));
    }

    @Transactional
    public Movimiento create(Usuario usuario, Long categoriaId, TipoMovimiento tipo,
                             BigDecimal importe, String descripcion, LocalDate fecha) {
        Categoria categoria = resolveCategoria(usuario, categoriaId);
        return movimientos.save(new Movimiento(usuario, categoria, tipo, importe, descripcion, fecha));
    }

    @Transactional
    public Movimiento update(Usuario usuario, Long id, Long categoriaId, TipoMovimiento tipo,
                             BigDecimal importe, String descripcion, LocalDate fecha) {
        Movimiento m = get(usuario, id);
        m.setCategoria(resolveCategoria(usuario, categoriaId));
        m.setTipo(tipo);
        m.setImporte(importe);
        m.setDescripcion(descripcion);
        m.setFecha(fecha);
        return m;
    }

    @Transactional
    public void delete(Usuario usuario, Long id) {
        Movimiento m = get(usuario, id);
        movimientos.delete(m);
    }

    private Categoria resolveCategoria(Usuario usuario, Long categoriaId) {
        if (categoriaId == null) return null;
        return categorias.findByIdAndUsuario(categoriaId, usuario)
            .orElseThrow(() -> new IllegalArgumentException("Categoría inválida: " + categoriaId));
    }

    public static class MovimientoNotFoundException extends RuntimeException {
        public MovimientoNotFoundException(Long id) { super("Movimiento no encontrado: " + id); }
    }
}
```

- [ ] `./mvnw -q compile` → SUCCESS
- [ ] Commit:

```bash
git add src/main/java/es/franciscorodalf/safeinvestor/movimientos/service/MovimientoService.java
git commit -m "feat: MovimientoService con CRUD y búsqueda paginada"
```

---

## Task 8 (F2.8): REST `CategoriaApiController` + DTOs

**Files:**
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/dto/CategoriaRequest.java`
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/dto/CategoriaResponse.java`
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/CategoriaApiController.java`

- [ ] `CategoriaRequest.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
    @NotBlank @Size(min = 1, max = 50) String nombre,
    @Pattern(regexp = "#[0-9A-Fa-f]{6}") String color,
    @Size(max = 50) String icono
) {}
```

- [ ] `CategoriaResponse.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.api.dto;

import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;

public record CategoriaResponse(Long id, String nombre, String color, String icono) {
    public static CategoriaResponse from(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getColor(), c.getIcono());
    }
}
```

- [ ] `CategoriaApiController.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.api;

import es.franciscorodalf.safeinvestor.movimientos.api.dto.CategoriaRequest;
import es.franciscorodalf.safeinvestor.movimientos.api.dto.CategoriaResponse;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaApiController {

    private final CategoriaService service;
    private final CurrentUser currentUser;

    public CategoriaApiController(CategoriaService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<CategoriaResponse> list() {
        return service.findAll(currentUser.get()).stream().map(CategoriaResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<CategoriaResponse> create(@Valid @RequestBody CategoriaRequest req) {
        var c = service.create(currentUser.get(), req.nombre(), req.color(), req.icono());
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoriaResponse.from(c));
    }

    @PutMapping("/{id}")
    public CategoriaResponse update(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        var c = service.update(currentUser.get(), id, req.nombre(), req.color(), req.icono());
        return CategoriaResponse.from(c);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(CategoriaService.CategoriaNotFoundException.class)
    public void handleNotFound() {}

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(CategoriaService.CategoriaDuplicadaException.class)
    public void handleDuplicate() {}
}
```

- [ ] `./mvnw -q compile` → SUCCESS
- [ ] Commit:

```bash
git add src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/dto src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/CategoriaApiController.java
git commit -m "feat: API REST /api/categorias con CRUD y validación"
```

---

## Task 9 (F2.9): REST `MovimientoApiController` + DTOs

**Files:**
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/dto/MovimientoRequest.java`
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/dto/MovimientoResponse.java`
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/MovimientoApiController.java`

- [ ] `MovimientoRequest.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.api.dto;

import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoRequest(
    Long categoriaId,
    @NotNull TipoMovimiento tipo,
    @NotNull @DecimalMin(value = "0.01", message = "El importe debe ser mayor que 0") BigDecimal importe,
    @Size(max = 200) String descripcion,
    @NotNull LocalDate fecha
) {}
```

- [ ] `MovimientoResponse.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.api.dto;

import es.franciscorodalf.safeinvestor.movimientos.domain.Movimiento;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoResponse(
    Long id, Long categoriaId, String categoriaNombre,
    TipoMovimiento tipo, BigDecimal importe, String descripcion, LocalDate fecha
) {
    public static MovimientoResponse from(Movimiento m) {
        Long catId = m.getCategoria() != null ? m.getCategoria().getId() : null;
        String catNombre = m.getCategoria() != null ? m.getCategoria().getNombre() : null;
        return new MovimientoResponse(m.getId(), catId, catNombre,
            m.getTipo(), m.getImporte(), m.getDescripcion(), m.getFecha());
    }
}
```

- [ ] `MovimientoApiController.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.api;

import es.franciscorodalf.safeinvestor.movimientos.api.dto.MovimientoRequest;
import es.franciscorodalf.safeinvestor.movimientos.api.dto.MovimientoResponse;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoApiController {

    private final MovimientoService service;
    private final CurrentUser currentUser;

    public MovimientoApiController(MovimientoService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<MovimientoResponse> list(
        @RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(defaultValue = "0") int page
    ) {
        return service.search(currentUser.get(), categoriaId, desde, hasta, page)
            .map(MovimientoResponse::from).getContent();
    }

    @GetMapping("/{id}")
    public MovimientoResponse get(@PathVariable Long id) {
        return MovimientoResponse.from(service.get(currentUser.get(), id));
    }

    @PostMapping
    public ResponseEntity<MovimientoResponse> create(@Valid @RequestBody MovimientoRequest req) {
        var m = service.create(currentUser.get(), req.categoriaId(), req.tipo(),
            req.importe(), req.descripcion(), req.fecha());
        return ResponseEntity.status(HttpStatus.CREATED).body(MovimientoResponse.from(m));
    }

    @PutMapping("/{id}")
    public MovimientoResponse update(@PathVariable Long id, @Valid @RequestBody MovimientoRequest req) {
        var m = service.update(currentUser.get(), id, req.categoriaId(), req.tipo(),
            req.importe(), req.descripcion(), req.fecha());
        return MovimientoResponse.from(m);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MovimientoService.MovimientoNotFoundException.class)
    public void handleNotFound() {}

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public void handleBadCategoria() {}
}
```

- [ ] `./mvnw -q compile` → SUCCESS
- [ ] Commit:

```bash
git add src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/dto/MovimientoRequest.java src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/dto/MovimientoResponse.java src/main/java/es/franciscorodalf/safeinvestor/movimientos/api/MovimientoApiController.java
git commit -m "feat: API REST /api/movimientos con CRUD, paginación y filtros"
```

---

## Task 10 (F2.10): Web controllers + plantillas

**Files (web side):**
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/web/form/MovimientoForm.java`
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/web/MovimientoWebController.java`
- `src/main/java/es/franciscorodalf/safeinvestor/movimientos/web/CategoriaWebController.java`
- `src/main/resources/templates/movimientos/list.html`
- `src/main/resources/templates/movimientos/form.html`
- `src/main/resources/templates/categorias/list.html`
- MODIFICAR `src/main/resources/templates/index.html` (añadir links a /movimientos y /categorias cuando hay sesión)

- [ ] `MovimientoForm.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.web.form;

import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MovimientoForm {

    private Long id;
    private Long categoriaId;
    @NotNull private TipoMovimiento tipo = TipoMovimiento.GASTO;
    @NotNull @DecimalMin(value = "0.01") private BigDecimal importe;
    private String descripcion;
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate fecha = LocalDate.now();

    public Long getId() { return id; }
    public Long getCategoriaId() { return categoriaId; }
    public TipoMovimiento getTipo() { return tipo; }
    public BigDecimal getImporte() { return importe; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFecha() { return fecha; }

    public void setId(Long id) { this.id = id; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
}
```

- [ ] `MovimientoWebController.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.web;

import es.franciscorodalf.safeinvestor.movimientos.domain.Movimiento;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import es.franciscorodalf.safeinvestor.movimientos.service.MovimientoService;
import es.franciscorodalf.safeinvestor.movimientos.web.form.MovimientoForm;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/movimientos")
public class MovimientoWebController {

    private final MovimientoService movimientos;
    private final CategoriaService categorias;
    private final CurrentUser currentUser;

    public MovimientoWebController(MovimientoService movimientos,
                                   CategoriaService categorias,
                                   CurrentUser currentUser) {
        this.movimientos = movimientos;
        this.categorias = categorias;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String list(
        @RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        var u = currentUser.get();
        var pageResult = movimientos.search(u, categoriaId, desde, hasta, page);
        model.addAttribute("movimientos", pageResult.getContent());
        model.addAttribute("categorias", categorias.findAll(u));
        model.addAttribute("currentCategoriaId", categoriaId);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("page", page);
        model.addAttribute("hasNext", pageResult.hasNext());
        model.addAttribute("hasPrev", pageResult.hasPrevious());
        return "movimientos/list";
    }

    @GetMapping("/nuevo")
    public String nuevoPage(Model model) {
        model.addAttribute("form", new MovimientoForm());
        model.addAttribute("categorias", categorias.findAll(currentUser.get()));
        return "movimientos/form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") MovimientoForm form,
                        BindingResult binding,
                        Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("categorias", categorias.findAll(currentUser.get()));
            return "movimientos/form";
        }
        movimientos.create(currentUser.get(), form.getCategoriaId(), form.getTipo(),
            form.getImporte(), form.getDescripcion(), form.getFecha());
        return "redirect:/movimientos";
    }

    @GetMapping("/{id}/editar")
    public String editarPage(@PathVariable Long id, Model model) {
        Movimiento m = movimientos.get(currentUser.get(), id);
        MovimientoForm form = new MovimientoForm();
        form.setId(m.getId());
        form.setCategoriaId(m.getCategoria() != null ? m.getCategoria().getId() : null);
        form.setTipo(m.getTipo());
        form.setImporte(m.getImporte());
        form.setDescripcion(m.getDescripcion());
        form.setFecha(m.getFecha());
        model.addAttribute("form", form);
        model.addAttribute("categorias", categorias.findAll(currentUser.get()));
        return "movimientos/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("form") MovimientoForm form,
                             BindingResult binding,
                             Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("categorias", categorias.findAll(currentUser.get()));
            return "movimientos/form";
        }
        movimientos.update(currentUser.get(), id, form.getCategoriaId(), form.getTipo(),
            form.getImporte(), form.getDescripcion(), form.getFecha());
        return "redirect:/movimientos";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id) {
        movimientos.delete(currentUser.get(), id);
        return "redirect:/movimientos";
    }
}
```

- [ ] `CategoriaWebController.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.web;

import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categorias")
public class CategoriaWebController {

    private final CategoriaService service;
    private final CurrentUser currentUser;

    public CategoriaWebController(CategoriaService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categorias", service.findAll(currentUser.get()));
        return "categorias/list";
    }

    @PostMapping
    public String crear(@RequestParam String nombre,
                        @RequestParam(required = false) String color) {
        service.create(currentUser.get(), nombre, color != null ? color : "#6B7280", null);
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
        return "redirect:/categorias";
    }
}
```

- [ ] `templates/movimientos/list.html`:

```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"><title>Movimientos — SafeInvestor</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-50 min-h-screen">
<nav class="bg-white shadow">
    <div class="max-w-5xl mx-auto px-4 py-3 flex justify-between items-center">
        <a href="/" class="text-xl font-bold text-blue-600">SafeInvestor</a>
        <div class="flex items-center gap-4 text-sm">
            <a href="/movimientos" class="text-blue-600 font-semibold">Movimientos</a>
            <a href="/categorias" class="text-gray-700 hover:text-blue-600">Categorías</a>
            <form th:action="@{/logout}" method="post" class="inline">
                <button class="text-red-600 hover:underline">Cerrar sesión</button>
            </form>
        </div>
    </div>
</nav>
<main class="max-w-5xl mx-auto px-4 py-6">
    <div class="flex justify-between items-center mb-4">
        <h1 class="text-2xl font-bold">Mis movimientos</h1>
        <a href="/movimientos/nuevo" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">+ Nuevo</a>
    </div>

    <form method="get" action="/movimientos" class="bg-white p-4 rounded shadow mb-4 grid grid-cols-1 md:grid-cols-4 gap-3 text-sm">
        <select name="categoriaId" class="border rounded px-2 py-1">
            <option value="">Todas las categorías</option>
            <option th:each="c : ${categorias}" th:value="${c.id}" th:text="${c.nombre}"
                    th:selected="${c.id == currentCategoriaId}"></option>
        </select>
        <input type="date" name="desde" th:value="${desde}" class="border rounded px-2 py-1">
        <input type="date" name="hasta" th:value="${hasta}" class="border rounded px-2 py-1">
        <button class="bg-gray-700 text-white rounded px-3 py-1">Filtrar</button>
    </form>

    <div class="bg-white rounded shadow overflow-hidden">
        <table class="w-full text-sm">
            <thead class="bg-gray-100 text-left">
                <tr><th class="p-3">Fecha</th><th>Categoría</th><th>Descripción</th><th class="text-right">Importe</th><th></th></tr>
            </thead>
            <tbody>
            <tr th:each="m : ${movimientos}" class="border-t hover:bg-gray-50">
                <td class="p-3" th:text="${#temporals.format(m.fecha, 'dd/MM/yyyy')}"></td>
                <td>
                    <span th:if="${m.categoria != null}"
                          class="inline-block w-3 h-3 rounded-full mr-1"
                          th:style="'background-color:' + ${m.categoria.color}"></span>
                    <span th:text="${m.categoria != null ? m.categoria.nombre : '—'}"></span>
                </td>
                <td th:text="${m.descripcion ?: ''}"></td>
                <td class="text-right font-mono"
                    th:classappend="${m.tipo.name() == 'GASTO'} ? 'text-red-600' : 'text-green-600'">
                    <span th:text="${m.tipo.name() == 'GASTO' ? '-' : '+'}"></span>
                    <span th:text="${#numbers.formatDecimal(m.importe, 1, 2)} + ' €'"></span>
                </td>
                <td class="text-right pr-3">
                    <a th:href="@{/movimientos/{id}/editar(id=${m.id})}" class="text-blue-600 text-xs">Editar</a>
                    <form th:action="@{/movimientos/{id}/borrar(id=${m.id})}" method="post" class="inline ml-2">
                        <button class="text-red-600 text-xs" onclick="return confirm('¿Borrar este movimiento?')">Borrar</button>
                    </form>
                </td>
            </tr>
            <tr th:if="${#lists.isEmpty(movimientos)}">
                <td colspan="5" class="p-6 text-center text-gray-500">No hay movimientos todavía. <a href="/movimientos/nuevo" class="text-blue-600">Crear el primero</a>.</td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="flex justify-between mt-4 text-sm">
        <a th:if="${hasPrev}" th:href="@{/movimientos(page=${page - 1}, categoriaId=${currentCategoriaId}, desde=${desde}, hasta=${hasta})}"
           class="text-blue-600">&larr; Anterior</a>
        <span></span>
        <a th:if="${hasNext}" th:href="@{/movimientos(page=${page + 1}, categoriaId=${currentCategoriaId}, desde=${desde}, hasta=${hasta})}"
           class="text-blue-600">Siguiente &rarr;</a>
    </div>
</main>
</body>
</html>
```

- [ ] `templates/movimientos/form.html`:

```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"><title>Movimiento — SafeInvestor</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-50 min-h-screen">
<nav class="bg-white shadow">
    <div class="max-w-5xl mx-auto px-4 py-3">
        <a href="/" class="text-xl font-bold text-blue-600">SafeInvestor</a>
    </div>
</nav>
<main class="max-w-md mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-4" th:text="${form.id == null ? 'Nuevo movimiento' : 'Editar movimiento'}">Movimiento</h1>
    <form th:action="${form.id == null ? '/movimientos' : '/movimientos/' + form.id}"
          th:object="${form}" method="post" class="bg-white p-6 rounded shadow space-y-3">
        <label class="block">
            <span class="text-sm text-gray-700">Tipo</span>
            <select th:field="*{tipo}" class="mt-1 w-full px-3 py-2 border rounded">
                <option value="GASTO">Gasto</option>
                <option value="INGRESO">Ingreso</option>
            </select>
        </label>
        <label class="block">
            <span class="text-sm text-gray-700">Importe (€)</span>
            <input th:field="*{importe}" type="number" step="0.01" min="0.01" required
                   class="mt-1 w-full px-3 py-2 border rounded">
            <span class="text-red-600 text-xs" th:if="${#fields.hasErrors('importe')}" th:errors="*{importe}"></span>
        </label>
        <label class="block">
            <span class="text-sm text-gray-700">Categoría</span>
            <select th:field="*{categoriaId}" class="mt-1 w-full px-3 py-2 border rounded">
                <option value="">— Sin categoría —</option>
                <option th:each="c : ${categorias}" th:value="${c.id}" th:text="${c.nombre}"></option>
            </select>
        </label>
        <label class="block">
            <span class="text-sm text-gray-700">Descripción</span>
            <input th:field="*{descripcion}" type="text" maxlength="200"
                   class="mt-1 w-full px-3 py-2 border rounded">
        </label>
        <label class="block">
            <span class="text-sm text-gray-700">Fecha</span>
            <input th:field="*{fecha}" type="date" required class="mt-1 w-full px-3 py-2 border rounded">
        </label>
        <div class="flex justify-between pt-2">
            <a href="/movimientos" class="text-gray-600 hover:underline">Cancelar</a>
            <button class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Guardar</button>
        </div>
    </form>
</main>
</body>
</html>
```

- [ ] `templates/categorias/list.html`:

```html
<!DOCTYPE html>
<html lang="es" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"><title>Categorías — SafeInvestor</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-50 min-h-screen">
<nav class="bg-white shadow">
    <div class="max-w-5xl mx-auto px-4 py-3 flex justify-between items-center">
        <a href="/" class="text-xl font-bold text-blue-600">SafeInvestor</a>
        <div class="flex items-center gap-4 text-sm">
            <a href="/movimientos" class="text-gray-700 hover:text-blue-600">Movimientos</a>
            <a href="/categorias" class="text-blue-600 font-semibold">Categorías</a>
        </div>
    </div>
</nav>
<main class="max-w-2xl mx-auto px-4 py-6">
    <h1 class="text-2xl font-bold mb-4">Mis categorías</h1>

    <form method="post" action="/categorias" class="bg-white p-4 rounded shadow mb-6 flex gap-2 items-end">
        <label class="flex-1">
            <span class="text-sm text-gray-700">Nombre</span>
            <input name="nombre" type="text" required maxlength="50" class="mt-1 w-full px-3 py-2 border rounded">
        </label>
        <label>
            <span class="text-sm text-gray-700">Color</span>
            <input name="color" type="color" value="#6B7280" class="mt-1 h-10 w-16 border rounded">
        </label>
        <button class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">+ Añadir</button>
    </form>

    <div class="bg-white rounded shadow divide-y">
        <div th:each="c : ${categorias}" class="flex items-center justify-between p-3">
            <div class="flex items-center gap-3">
                <span class="inline-block w-4 h-4 rounded-full" th:style="'background-color:' + ${c.color}"></span>
                <span th:text="${c.nombre}"></span>
            </div>
            <form th:action="@{/categorias/{id}/borrar(id=${c.id})}" method="post">
                <button class="text-red-600 text-sm" onclick="return confirm('¿Borrar esta categoría?')">Borrar</button>
            </form>
        </div>
    </div>
</main>
</body>
</html>
```

- [ ] Modificar `templates/index.html` — añadir links a /movimientos y /categorias dentro del bloque autenticado. Reemplazar el `<p>Hola...</p>` dentro de `sec:authorize="isAuthenticated()"` con:

```html
        <div sec:authorize="isAuthenticated()" xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
            <p class="mb-4">Hola <span sec:authentication="name" class="font-semibold"></span>.</p>
            <a href="/movimientos" class="bg-blue-600 text-white px-6 py-2 rounded hover:bg-blue-700">Mis movimientos</a>
            <a href="/categorias" class="ml-2 border border-blue-600 text-blue-600 px-6 py-2 rounded hover:bg-blue-50">Categorías</a>
        </div>
```

- [ ] `./mvnw -q compile` → SUCCESS
- [ ] Commit:

```bash
git add src/main/java/es/franciscorodalf/safeinvestor/movimientos/web src/main/resources/templates/movimientos src/main/resources/templates/categorias src/main/resources/templates/index.html
git commit -m "feat: controllers web y plantillas Thymeleaf para movimientos y categorías"
```

---

## Task 11 (F2.11): Tests integración

**Files:**
- `src/test/java/es/franciscorodalf/safeinvestor/movimientos/service/CategoriaServiceTest.java`
- `src/test/java/es/franciscorodalf/safeinvestor/movimientos/service/MovimientoServiceTest.java`
- `src/test/java/es/franciscorodalf/safeinvestor/movimientos/api/MovimientoApiControllerTest.java`

- [ ] `CategoriaServiceTest.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoriaServiceTest {

    @Autowired UsuarioService usuarioService;
    @Autowired CategoriaService service;

    @Test
    void usuarioRegistradoTieneCategoriasDefault() {
        Usuario u = usuarioService.register("cat-test@x.com", "Test", "password123");
        var lista = service.findAll(u);
        assertEquals(11, lista.size(),
            "Cada usuario nuevo debería tener 11 categorías default");
        assertTrue(lista.stream().anyMatch(c -> c.getNombre().equals("Comida")));
        assertTrue(lista.stream().anyMatch(c -> c.getNombre().equals("Nómina")));
    }

    @Test
    void noPermiteNombresDuplicados() {
        Usuario u = usuarioService.register("dup-cat@x.com", "Dup", "password123");
        service.create(u, "Personal", "#FF0000", null);
        assertThrows(CategoriaService.CategoriaDuplicadaException.class,
            () -> service.create(u, "PERSONAL", "#00FF00", null));
    }
}
```

- [ ] `MovimientoServiceTest.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MovimientoServiceTest {

    @Autowired UsuarioService usuarioService;
    @Autowired CategoriaService categoriaService;
    @Autowired MovimientoService service;

    @Test
    void creaYBuscaMovimientos() {
        Usuario u = usuarioService.register("mov-test@x.com", "Test", "password123");
        var comida = categoriaService.findAll(u).stream()
            .filter(c -> c.getNombre().equals("Comida")).findFirst().orElseThrow();

        service.create(u, comida.getId(), TipoMovimiento.GASTO,
            new BigDecimal("12.50"), "Almuerzo", LocalDate.now());
        service.create(u, null, TipoMovimiento.INGRESO,
            new BigDecimal("100.00"), "Regalo", LocalDate.now());

        var page = service.search(u, null, null, null, 0);
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void noPermiteAccederAMovimientoDeOtroUsuario() {
        Usuario u1 = usuarioService.register("u1@x.com", "U1", "password123");
        Usuario u2 = usuarioService.register("u2@x.com", "U2", "password123");
        var m = service.create(u1, null, TipoMovimiento.GASTO,
            new BigDecimal("5.00"), null, LocalDate.now());
        assertThrows(MovimientoService.MovimientoNotFoundException.class,
            () -> service.get(u2, m.getId()));
    }
}
```

- [ ] `MovimientoApiControllerTest.java`:

```java
package es.franciscorodalf.safeinvestor.movimientos.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MovimientoApiControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioService usuarioService;

    @BeforeEach
    void crearUsuario() {
        try { usuarioService.register("apiuser@x.com", "Api", "password123"); }
        catch (Exception ignored) {}
    }

    @Test
    @WithMockUser(username = "apiuser@x.com")
    void creaYListaMovimiento() throws Exception {
        var body = Map.of(
            "tipo", "GASTO",
            "importe", "25.50",
            "descripcion", "Test",
            "fecha", "2026-06-05"
        );
        mvc.perform(post("/api/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.importe").value(25.50));

        mvc.perform(get("/api/movimientos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].tipo").value("GASTO"));
    }

    @Test
    void sinAuthDevuelve401() throws Exception {
        mvc.perform(get("/api/movimientos"))
            .andExpect(status().isUnauthorized());
    }
}
```

- [ ] Ejecutar todos los tests:

```bash
./mvnw test
```
Expected: ahora hay 13 tests (8 previos + 5 nuevos). Todos verde.

- [ ] Commit:

```bash
git add src/test/java/es/franciscorodalf/safeinvestor/movimientos
git commit -m "test: tests de integración para CategoriaService, MovimientoService y MovimientoApiController"
```

---

## Task 12 (F2.12): Verificación manual + push + redeploy en Render

- [ ] Levantar localmente:

```bash
docker compose up -d
./mvnw spring-boot:run
```

En otra terminal:
```bash
# Registro de un usuario fresco
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"phase2@example.com","nombre":"Fase2","password":"password123"}' | jq -r .token)

# Ver categorías default (debe devolver 11)
curl -s http://localhost:8080/api/categorias -H "Authorization: Bearer $TOKEN" | jq 'length'

# Crear un gasto
curl -s -X POST http://localhost:8080/api/movimientos \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"tipo":"GASTO","importe":45.30,"descripcion":"Súper","fecha":"2026-06-05"}' \
  -w "\nHTTP: %{http_code}\n"

# Listar movimientos
curl -s http://localhost:8080/api/movimientos -H "Authorization: Bearer $TOKEN" | jq
```

Abrir navegador en `http://localhost:8080/login`, login con `phase2@example.com / password123`, ver `/movimientos` con la entrada creada. Probar `/movimientos/nuevo` desde la UI. Parar la app (`Ctrl+C`).

- [ ] Push y verificar CI:

```bash
git push origin main
```

Esperar a que GitHub Actions ponga verde (~1 min) y que Render redespliegue (~3-4 min).

- [ ] Verificar la demo pública:

```bash
# Health
curl -s https://safeinvestor.onrender.com/actuator/health

# Crear cuenta de demo nueva (la BD de Render es separada)
TOKEN=$(curl -s -X POST https://safeinvestor.onrender.com/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo-f2@example.com","nombre":"DemoF2","password":"password123"}' | jq -r .token)

# Ver categorías default
curl -s https://safeinvestor.onrender.com/api/categorias -H "Authorization: Bearer $TOKEN" | jq

# Crear movimiento
curl -s -X POST https://safeinvestor.onrender.com/api/movimientos \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"tipo":"INGRESO","importe":1200.00,"descripcion":"Nómina","fecha":"2026-06-05"}' \
  -w "\nHTTP: %{http_code}\n"
```

- [ ] Actualizar `README.md` — modificar el roadmap: marcar `[x]` la Fase 2 y añadir una nota a la sección "Endpoints" con `/api/categorias` y `/api/movimientos`. Después:

```bash
git add README.md
git commit -m "docs: marcar Fase 2 completada en el roadmap y añadir endpoints de movimientos"
git push origin main
```

---

## Phase 2 done — exit criteria

- [ ] Las 12 tasks committed en orden.
- [ ] `./mvnw verify` verde local con 13 tests pasando.
- [ ] App local: registro crea usuario + 11 categorías default; POST/GET /api/movimientos funciona; /movimientos en navegador muestra UI con filtros, paginación, formulario.
- [ ] CI verde en `main`.
- [ ] Demo Render redesplegada y respondiendo en endpoints nuevos.
- [ ] README actualizado.

Cuando los 5 estén tickeados, Fase 2 cerrada. Fase 3 (objetivos de ahorro + tareas financieras) tiene su propio plan.
