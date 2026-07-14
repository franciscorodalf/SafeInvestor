package es.franciscorodalf.safeinvestor.tickets.service;

/**
 * Abstracción sobre el proveedor de visión que analiza la foto del ticket.
 *
 * Implementaciones actuales:
 *   - {@link GeminiVisionProvider}   (Google, free tier — default)
 *   - {@link AnthropicVisionProvider} (Claude, de pago)
 *
 * {@link TicketVisionService} elige el proveedor según la propiedad
 * {@code vision.provider} (auto | gemini | anthropic). En modo {@code auto}
 * gana el primero configurado, con preferencia por Gemini al ser gratuito.
 */
public interface VisionProvider {

    /** Identificador para la propiedad vision.provider ("gemini", "anthropic"). */
    String id();

    /** true si hay API key y el proveedor puede usarse. */
    boolean isConfigured();

    /**
     * Envía la imagen + prompt al modelo y devuelve su respuesta de texto cruda
     * (el parseo del JSON lo hace el caller, es igual para todos los proveedores).
     */
    String analyze(byte[] imageBytes, String mimeType, String prompt);
}
