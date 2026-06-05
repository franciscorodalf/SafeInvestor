package es.franciscorodalf.safeinvestor.movimientos.api.dto;

import es.franciscorodalf.safeinvestor.movimientos.domain.Movimiento;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovimientoResponse(
    Long id, Long categoriaId, String categoriaNombre,
    TipoMovimiento tipo, BigDecimal importe, String descripcion, LocalDate fecha
) {
    public static MovimientoResponse from(Movimiento m) {
        Long catId = m.getCategoria() != null ? m.getCategoria().getId() : null;
        String catNombre = m.getCategoria() != null ? m.getCategoria().getNombre() : null;
        return new MovimientoResponse(m.getId(), catId, catNombre,
            m.getTipo(), m.getImporte(), m.getDescripcion(), m.getFecha());
    }
}
