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
