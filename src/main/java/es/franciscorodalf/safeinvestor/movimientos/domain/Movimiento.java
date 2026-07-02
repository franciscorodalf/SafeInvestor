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

    /**
     * ID externo del movimiento en el sistema origen (p.ej. {@code transactionId}
     * de GoCardless). Se usa para dedup entre syncs sucesivos. Null = movimiento
     * creado manualmente por el usuario.
     */
    @Column(name = "external_id", length = 200)
    private String externalId;

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

    public String getExternalId() { return externalId; }

    public void setCategoria(Categoria c) { this.categoria = c; }
    public void setTipo(TipoMovimiento t) { this.tipo = t; }
    public void setImporte(BigDecimal i) { this.importe = i; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public void setFecha(LocalDate f) { this.fecha = f; }
    public void setExternalId(String id) { this.externalId = id; }
}
