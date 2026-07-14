package es.franciscorodalf.safeinvestor.tickets.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del cliente de Google Gemini. El free tier de los modelos
 * Flash incluye visión (10 req/min, 1.500 req/día) sin tarjeta — API key
 * gratuita en https://aistudio.google.com.
 */
@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        String baseUrl,
        String model
) {
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
