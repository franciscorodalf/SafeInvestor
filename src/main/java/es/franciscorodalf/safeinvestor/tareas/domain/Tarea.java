package es.franciscorodalf.safeinvestor.tareas.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "completada_at")
    private OffsetDateTime completadaAt;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    public Tarea() {}

    public Tarea(Usuario usuario, String titulo, String descripcion, LocalDate fechaVencimiento) {
        this.usuario = usuario;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaVencimiento = fechaVencimiento;
    }

    public boolean isCompletada() { return completadaAt != null; }

    public boolean isVencida() {
        return !isCompletada()
            && fechaVencimiento != null
            && fechaVencimiento.isBefore(LocalDate.now());
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public OffsetDateTime getCompletadaAt() { return completadaAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setTitulo(String t) { this.titulo = t; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }
    public void setCompletadaAt(OffsetDateTime t) { this.completadaAt = t; }
}
