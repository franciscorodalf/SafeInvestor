package es.franciscorodalf.safeinvestor.tickets.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del cliente de Anthropic (Claude Vision). Se lee de
 * {@code application.yml} bajo el prefijo {@code anthropic}. Si {@code apiKey}
 * está vacío, el proveedor queda deshabilitado sin romper el arranque.
 */
@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(
        String apiKey,
        String baseUrl,
        String model
) {
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
