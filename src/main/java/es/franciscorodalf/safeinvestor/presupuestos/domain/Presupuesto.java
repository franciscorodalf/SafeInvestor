package es.franciscorodalf.safeinvestor.presupuestos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Presupuesto mensual por categoría: tope que el usuario se autoimpone.
 * Único por (usuario, categoría, año, mes) — constraint a nivel de BBDD.
 */
@Entity
@Table(name = "presupuestos")
public class Presupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "anio", nullable = false)
    private int anio;

    @Column(name = "mes", nullable = false)
    private int mes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal limite;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected Presupuesto() {}

    public Presupuesto(Usuario usuario, Categoria categoria, int anio, int mes, BigDecimal limite) {
        this.usuario = usuario;
        this.categoria = categoria;
        this.anio = anio;
        this.mes = mes;
        this.limite = limite;
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Categoria getCategoria() { return categoria; }
    public int getAnio() { return anio; }
    public int getMes() { return mes; }
    public BigDecimal getLimite() { return limite; }
    public void setLimite(BigDecimal limite) { this.limite = limite; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
