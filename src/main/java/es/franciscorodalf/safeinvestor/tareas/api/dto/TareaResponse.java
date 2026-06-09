package es.franciscorodalf.safeinvestor.tareas.api.dto;

import es.franciscorodalf.safeinvestor.tareas.domain.Tarea;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record TareaResponse(
    Long id, String titulo, String descripcion,
    LocalDate fechaVencimiento, boolean completada, OffsetDateTime completadaAt,
    boolean vencida
) {
    public static TareaResponse from(Tarea t) {
        return new TareaResponse(t.getId(), t.getTitulo(), t.getDescripcion(),
            t.getFechaVencimiento(), t.isCompletada(), t.getCompletadaAt(), t.isVencida());
    }
}
