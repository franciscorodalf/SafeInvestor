package es.franciscorodalf.safeinvestor.tips;

/**
 * Tip educativo de economía personal. Inmutable, cargado desde tips.json al arrancar.
 */
public record Tip(
        int id,
        String categoria,
        String titulo,
        String resumen,
        String detalle
) {}
