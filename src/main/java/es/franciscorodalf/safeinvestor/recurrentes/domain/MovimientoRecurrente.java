package es.franciscorodalf.safeinvestor.recurrentes.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Plantilla de movimiento que se materializa periódicamente.
 *
 * Cuando el job @Scheduled diario corre, busca todas las recurrencias
 * activas con {@code proximaEjecucion <= hoy}, crea el movimiento real
 * y avanza {@code proximaEjecucion} según la frecuencia.
 */
@Entity
@Table(name = "movimientos_recurrentes")
public class MovimientoRecurrente {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Frecuencia frecuencia;

    @Column(name = "proxima_ejecucion", nullable = false)
    private LocalDate proximaEjecucion;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected MovimientoRecurrente() {}

    public MovimientoRecurrente(Usuario usuario, Categoria categoria, TipoMovimiento tipo,
                                BigDecimal importe, String descripcion, Frecuencia frecuencia,
                                LocalDate proximaEjecucion) {
        this.usuario = usuario;
        this.categoria = categoria;
        this.tipo = tipo;
        this.importe = importe;
        this.descripcion = descripcion;
        this.frecuencia = frecuencia;
        this.proximaEjecucion = proximaEjecucion;
    }

    /** Avanza la fecha al siguiente periodo según la frecuencia. */
    public void avanzarSiguienteEjecucion() {
        this.proximaEjecucion = frecuencia.next(proximaEjecucion);
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria c) { this.categoria = c; }
    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento t) { this.tipo = t; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal i) { this.importe = i; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public Frecuencia getFrecuencia() { return frecuencia; }
    public void setFrecuencia(Frecuencia f) { this.frecuencia = f; }
    public LocalDate getProximaEjecucion() { return proximaEjecucion; }
    public void setProximaEjecucion(LocalDate d) { this.proximaEjecucion = d; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean a) { this.activo = a; }
}
