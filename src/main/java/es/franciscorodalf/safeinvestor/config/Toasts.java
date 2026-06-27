package es.franciscorodalf.safeinvestor.config;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Helper para emitir flash messages tras un redirect.
 *
 * El fragmento {@code fragments/toasts :: toasts} renderiza los atributos
 * {@code flashSuccess}, {@code flashError} y {@code flashInfo} como toasts
 * flotantes con auto-dismiss. Los controllers solo tienen que llamar a
 * {@link #success}/{@link #error}/{@link #info} con el RedirectAttributes.
 *
 * Ventaja sobre query params: permite mensajes parametrizados sin ensuciar
 * la URL y se borra solo (no se queda al refrescar).
 */
public final class Toasts {

    public static final String FLASH_SUCCESS = "flashSuccess";
    public static final String FLASH_ERROR   = "flashError";
    public static final String FLASH_INFO    = "flashInfo";

    private Toasts() {}

    public static void success(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute(FLASH_SUCCESS, msg);
    }

    public static void error(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute(FLASH_ERROR, msg);
    }

    public static void info(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute(FLASH_INFO, msg);
    }
}
