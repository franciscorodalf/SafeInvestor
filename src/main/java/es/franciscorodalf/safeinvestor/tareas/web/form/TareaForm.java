package es.franciscorodalf.safeinvestor.tareas.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class TareaForm {

    private Long id;
    @NotBlank @Size(max = 150) private String titulo;
    @Size(max = 500) private String descripcion;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate fechaVencimiento;

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }

    public void setId(Long id) { this.id = id; }
    public void setTitulo(String t) { this.titulo = t; }
    public void setDescripcion(String d) { this.descripcion = d; }
    public void setFechaVencimiento(LocalDate f) { this.fechaVencimiento = f; }
}
