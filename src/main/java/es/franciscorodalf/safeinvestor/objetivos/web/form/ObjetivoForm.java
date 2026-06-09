package es.franciscorodalf.safeinvestor.objetivos.web.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ObjetivoForm {

    private Long id;
    @NotBlank @Size(max = 100) private String nombre;
    @NotNull @DecimalMin("0.01") private BigDecimal importeObjetivo;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate fechaLimite;
    private String color = "#3B82F6";

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public BigDecimal getImporteObjetivo() { return importeObjetivo; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public String getColor() { return color; }

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setImporteObjetivo(BigDecimal i) { this.importeObjetivo = i; }
    public void setFechaLimite(LocalDate f) { this.fechaLimite = f; }
    public void setColor(String c) { this.color = c; }
}
