package es.franciscorodalf.safeinvestor.objetivos.api.dto;

import es.franciscorodalf.safeinvestor.objetivos.domain.Objetivo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ObjetivoResponse(
    Long id,
    String nombre,
    BigDecimal importeObjetivo,
    BigDecimal importeAhorrado,
    BigDecimal porcentaje,
    LocalDate fechaLimite,
    String color,
    boolean completado,
    OffsetDateTime completadoAt
) {
    public static ObjetivoResponse from(Objetivo o) {
        return new ObjetivoResponse(
            o.getId(), o.getNombre(), o.getImporteObjetivo(), o.getImporteAhorrado(),
            o.getPorcentaje(), o.getFechaLimite(), o.getColor(),
            o.isCompletado(), o.getCompletadoAt()
        );
    }
}
