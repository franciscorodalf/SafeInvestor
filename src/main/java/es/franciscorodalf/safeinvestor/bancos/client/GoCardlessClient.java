package es.franciscorodalf.safeinvestor.bancos.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP para la API de GoCardless Bank Account Data (Nordigen).
 *
 * Endpoints usados:
 *   - POST /token/new       → obtener access_token
 *   - GET  /institutions/   → listar bancos por país
 *   - POST /requisitions/   → crear consentimiento + link para el usuario
 *   - GET  /requisitions/id → obtener estado y lista de accounts
 *   - GET  /accounts/id/details/       → IBAN, nombre, currency
 *   - GET  /accounts/id/transactions/  → transacciones booked + pending
 *
 * Token: se cachea en memoria y se renueva 5 min antes de expirar. Como esta
 * app es single-instance en Render, un cache local es suficiente.
 *
 * Solo se registra como bean si {@code gocardless.enabled=true} y hay
 * credenciales — así el proyecto compila y arranca aunque el usuario no
 * haya obtenido credenciales todavía.
 */
@Component
@Configuration
@EnableConfigurationProperties(GoCardlessProperties.class)
public class GoCardlessClient {

    private static final Logger log = LoggerFactory.getLogger(GoCardlessClient.class);

    private final GoCardlessProperties props;
    private final RestClient http;
    private volatile String cachedAccessToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    public GoCardlessClient(GoCardlessProperties props) {
        this.props = props;
        this.http = props.isConfigured()
                ? RestClient.builder().baseUrl(props.baseUrl()).build()
                : null;
    }

    public boolean isConfigured() {
        return props.isConfigured() && http != null;
    }

    public GoCardlessProperties getProperties() {
        return props;
    }

    // ============================================================
    // Token
    // ============================================================

    private synchronized String accessToken() {
        if (cachedAccessToken != null && Instant.now().isBefore(tokenExpiresAt.minusSeconds(300))) {
            return cachedAccessToken;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> res = http.post()
                .uri("/token/new/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "secret_id",  props.secretId(),
                        "secret_key", props.secretKey()))
                .retrieve()
                .body(Map.class);
        if (res == null || res.get("access") == null) {
            throw new GoCardlessException("Respuesta inesperada al obtener token");
        }
        String access = (String) res.get("access");
        int expiresSec = ((Number) res.getOrDefault("access_expires", 86_400)).intValue();
        this.cachedAccessToken = access;
        this.tokenExpiresAt = Instant.now().plusSeconds(expiresSec);
        log.info("GoCardless: token obtenido, caduca en {}s", expiresSec);
        return access;
    }

    private RestClient.RequestHeadersSpec<?> authGet(String uri) {
        return http.get().uri(uri).header("Authorization", "Bearer " + accessToken());
    }

    private RestClient.RequestBodySpec authPost(String uri) {
        return http.post().uri(uri)
                .header("Authorization", "Bearer " + accessToken())
                .contentType(MediaType.APPLICATION_JSON);
    }

    // ============================================================
    // Institutions
    // ============================================================

    /** Lista de bancos disponibles en un país. Añade el banco de sandbox al principio siempre. */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listInstitutions(String country) {
        List<Map<String, Object>> real = authGet("/institutions/?country=" + country.toUpperCase())
                .retrieve()
                .body(List.class);
        if (real == null) real = List.of();
        // Añade sandbox al principio si no está
        boolean hasSandbox = real.stream().anyMatch(i -> "SANDBOXFINANCE_SFIN0000".equals(i.get("id")));
        if (!hasSandbox) {
            Map<String, Object> sandbox = Map.of(
                    "id",     "SANDBOXFINANCE_SFIN0000",
                    "name",   "Sandbox (datos de prueba)",
                    "logo",   "",
                    "bic",    "SFIN",
                    "transaction_total_days", "90"
            );
            var out = new java.util.ArrayList<Map<String, Object>>(real.size() + 1);
            out.add(sandbox);
            out.addAll(real);
            return out;
        }
        return real;
    }

    // ============================================================
    // Requisitions (consentimientos)
    // ============================================================

    /** Crea un requisition. Devuelve el mapa con {id, link, ...}. El link es la URL a la que redirigir al usuario. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> createRequisition(String institutionId, String reference) {
        Map<String, Object> body = Map.of(
                "redirect",       props.redirectUrl(),
                "institution_id", institutionId,
                "reference",      reference,
                "user_language",  "ES"
        );
        Map<String, Object> res = authPost("/requisitions/").body(body).retrieve().body(Map.class);
        if (res == null || res.get("id") == null || res.get("link") == null) {
            throw new GoCardlessException("Respuesta inesperada al crear requisition");
        }
        return res;
    }

    /** Devuelve estado + accounts de un requisition. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getRequisition(String requisitionId) {
        Map<String, Object> res = authGet("/requisitions/" + requisitionId + "/").retrieve().body(Map.class);
        if (res == null) throw new GoCardlessException("Requisition no encontrada");
        return res;
    }

    public void deleteRequisition(String requisitionId) {
        try {
            http.delete().uri("/requisitions/" + requisitionId + "/")
                    .header("Authorization", "Bearer " + accessToken())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Error borrando requisition {}: {}", requisitionId, e.getMessage());
        }
    }

    // ============================================================
    // Accounts + transactions
    // ============================================================

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAccountDetails(String accountId) {
        Map<String, Object> res = authGet("/accounts/" + accountId + "/details/").retrieve().body(Map.class);
        if (res == null) throw new GoCardlessException("Cuenta no encontrada");
        return res;
    }

    /**
     * Devuelve las transacciones (booked + pending) de una cuenta.
     * Nordigen limita la ventana normalmente a 90 días.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getAccountTransactions(String accountId) {
        Map<String, Object> res = authGet("/accounts/" + accountId + "/transactions/").retrieve().body(Map.class);
        if (res == null) throw new GoCardlessException("No se pudieron leer las transacciones");
        return res;
    }

    // ============================================================
    // Excepción
    // ============================================================

    public static class GoCardlessException extends RuntimeException {
        public GoCardlessException(String msg) { super(msg); }
        public GoCardlessException(String msg, Throwable cause) { super(msg, cause); }
    }

    @Bean
    public String goCardlessInit() {
        if (props.isConfigured()) {
            log.info("GoCardless habilitado. Base URL: {}", props.baseUrl());
        } else {
            log.info("GoCardless deshabilitado (enabled={}, secretId={}). Los endpoints /bancos mostrarán 'no configurado'.",
                    props.enabled(), props.secretId() != null && !props.secretId().isBlank() ? "presente" : "ausente");
        }
        return "gocardless-init";
    }
}
