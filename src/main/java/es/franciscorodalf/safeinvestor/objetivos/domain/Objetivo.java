package es.franciscorodalf.safeinvestor.objetivos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "objetivos")
public class Objetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "importe_objetivo", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeObjetivo;

    @Column(name = "importe_ahorrado", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeAhorrado = BigDecimal.ZERO;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    @Column(nullable = false, length = 7)
    private String color = "#3B82F6";

    @Column(name = "completado_at")
    private OffsetDateTime completadoAt;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    public Objetivo() {}

    public Objetivo(Usuario usuario, String nombre, BigDecimal importeObjetivo,
                    LocalDate fechaLimite, String color) {
        this.usuario = usuario;
        this.nombre = nombre;
        this.importeObjetivo = importeObjetivo;
        this.fechaLimite = fechaLimite;
        if (color != null) this.color = color;
    }

    /** Devuelve el porcentaje completado (0-100) redondeado a 1 decimal. */
    public BigDecimal getPorcentaje() {
        if (importeObjetivo == null || importeObjetivo.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal pct = importeAhorrado.multiply(BigDecimal.valueOf(100))
            .divide(importeObjetivo, 1, RoundingMode.HALF_UP);
        return pct.min(BigDecimal.valueOf(100));
    }

    public boolean isCompletado() { return completadoAt != null; }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getNombre() { return nombre; }
    public BigDecimal getImporteObjetivo() { return importeObjetivo; }
    public BigDecimal getImporteAhorrado() { return importeAhorrado; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public String getColor() { return color; }
    public OffsetDateTime getCompletadoAt() { return completadoAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setImporteObjetivo(BigDecimal i) { this.importeObjetivo = i; }
    public void setImporteAhorrado(BigDecimal i) { this.importeAhorrado = i; }
    public void setFechaLimite(LocalDate f) { this.fechaLimite = f; }
    public void setColor(String c) { this.color = c; }
    public void setCompletadoAt(OffsetDateTime t) { this.completadoAt = t; }
}
