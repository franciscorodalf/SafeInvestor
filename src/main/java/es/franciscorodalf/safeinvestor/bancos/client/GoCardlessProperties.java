package es.franciscorodalf.safeinvestor.bancos.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de GoCardless (Nordigen). Leída de {@code application.yml}
 * bajo el prefijo {@code gocardless}. Si {@code enabled=false} o faltan
 * las credenciales, el cliente HTTP no se registra y los endpoints /bancos
 * muestran un banner de \"no configurado\".
 */
@ConfigurationProperties(prefix = "gocardless")
public record GoCardlessProperties(
        boolean enabled,
        String secretId,
        String secretKey,
        String baseUrl,
        String redirectUrl,
        String defaultCountry,
        String syncCron
) {
    public boolean isConfigured() {
        return enabled && secretId != null && !secretId.isBlank()
                && secretKey != null && !secretKey.isBlank();
    }
}
