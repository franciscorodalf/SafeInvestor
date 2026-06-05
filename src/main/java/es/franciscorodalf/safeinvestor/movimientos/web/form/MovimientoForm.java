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
