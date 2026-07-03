package es.franciscorodalf.safeinvestor.tickets.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del cliente de Anthropic (Claude Vision). Se lee de
 * {@code application.yml} bajo el prefijo {@code anthropic}. Si {@code apiKey}
 * está vacío, la feature de OCR se muestra como "no configurada" en la UI
 * sin romper el arranque.
 */
@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(
        String apiKey,
        String baseUrl,
        String model,
        long maxImageBytes
) {
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
