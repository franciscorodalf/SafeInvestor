package es.franciscorodalf.safeinvestor.recurrentes.domain;

import java.time.LocalDate;

/** Frecuencias soportadas para movimientos recurrentes. */
public enum Frecuencia {
    DIARIA,
    SEMANAL,
    MENSUAL,
    ANUAL;

    /** Devuelve la siguiente fecha de ejecución a partir de la actual. */
    public LocalDate next(LocalDate from) {
        return switch (this) {
            case DIARIA  -> from.plusDays(1);
            case SEMANAL -> from.plusWeeks(1);
            case MENSUAL -> from.plusMonths(1);
            case ANUAL   -> from.plusYears(1);
        };
    }
}
