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
