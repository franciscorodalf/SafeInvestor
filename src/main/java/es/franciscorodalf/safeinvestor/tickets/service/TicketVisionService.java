package es.franciscorodalf.safeinvestor.tickets.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracción de datos estructurados de un ticket a partir de su foto usando
 * la Messages API de Anthropic (Claude con capacidades de visión).
 *
 * El flujo es:
 *   1. Recibe bytes de la imagen y su tipo MIME.
 *   2. Construye el request con dos bloques: un bloque {@code image} en base64
 *      y un bloque {@code text} con el prompt de extracción.
 *   3. Le pide a Claude que devuelva EXCLUSIVAMENTE un JSON con la estructura
 *      que le dictamos.
 *   4. Parsea el JSON (aunque venga con ruido rodeándolo) y lo convierte en
 *      un {@link TicketExtraction} manejable por el controller.
 *
 * Si {@link AnthropicProperties#isConfigured()} devuelve false, el servicio
 * lanza {@link IllegalStateException}. El controller lo captura y muestra el
 * banner de "no configurado" en la UI.
 */
@Service
@Configuration
@EnableConfigurationProperties(AnthropicProperties.class)
public class TicketVisionService {

    private static final Logger log = LoggerFactory.getLogger(TicketVisionService.class);

    private static final String PROMPT = """
            Analiza esta imagen de un ticket de compra y devuelve EXCLUSIVAMENTE un JSON
            con esta estructura, sin texto adicional ni markdown:

            {
              "fecha": "YYYY-MM-DD",
              "comercio": "string (nombre del comercio, ej. 'Mercadona', 'Amazon')",
              "importe_total": number (positivo, en unidades enteras y decimales),
              "moneda": "EUR" | "USD" | "GBP" | "otra",
              "resumen": "string (1 línea, qué compró resumido — ej. 'Compra semanal', 'Café y bollo')",
              "confianza": integer 0-100 (tu confianza en la extracción)
            }

            Reglas:
            - Si no puedes leer la fecha, usa la fecha de hoy.
            - Si el ticket muestra un total con impuestos, devuélvelo tal cual.
            - Si es un email de confirmación de compra online (Amazon, AliExpress...),
              trátalo como un ticket normal.
            - Si la imagen NO es un ticket (paisaje, retrato, texto random), devuelve:
              {"error": "descripción del problema"}
            """;

    private final AnthropicProperties props;
    private final RestClient http;
    private final ObjectMapper json = new ObjectMapper();

    public TicketVisionService(AnthropicProperties props) {
        this.props = props;
        this.http = props.isConfigured()
                ? RestClient.builder().baseUrl(props.baseUrl()).build()
                : null;
    }

    public boolean isConfigured() {
        return props.isConfigured() && http != null;
    }

    /** Extrae los datos del ticket. */
    public TicketExtraction extract(byte[] imageBytes, String mimeType) {
        if (!isConfigured()) {
            throw new IllegalStateException("Claude Vision no está configurado");
        }
        if (imageBytes.length > props.maxImageBytes()) {
            throw new IllegalArgumentException("La imagen es demasiado grande");
        }
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String mt = normalizeMime(mimeType);

        Map<String, Object> req = Map.of(
                "model", props.model(),
                "max_tokens", 512,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", List.of(
                                Map.of(
                                        "type", "image",
                                        "source", Map.of(
                                                "type", "base64",
                                                "media_type", mt,
                                                "data", base64)),
                                Map.of("type", "text", "text", PROMPT))
                ))
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> res = http.post()
                .uri("/messages")
                .header("x-api-key", props.apiKey())
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .body(Map.class);

        if (res == null) throw new VisionException("Respuesta vacía de Anthropic");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) res.get("content");
        if (content == null || content.isEmpty()) {
            throw new VisionException("Anthropic no devolvió contenido");
        }
        String text = String.valueOf(content.get(0).get("text"));
        return parseJson(text);
    }

    /** Extrae el JSON válido dentro de la respuesta (tolerante a texto rodeando). */
    private TicketExtraction parseJson(String raw) {
        String jsonText = raw.trim();
        // Si el modelo devolvió con ```json ... ``` u otro wrapping, extraemos el objeto
        Matcher m = Pattern.compile("\\{[\\s\\S]*\\}").matcher(jsonText);
        if (m.find()) jsonText = m.group();

        try {
            JsonNode node = json.readTree(jsonText);
            if (node.hasNonNull("error")) {
                return TicketExtraction.error(node.get("error").asText());
            }
            String fechaStr = node.path("fecha").asText(null);
            LocalDate fecha = fechaStr != null && !fechaStr.isBlank()
                    ? tryParseDate(fechaStr) : LocalDate.now();
            String comercio = node.path("comercio").asText("");
            BigDecimal importe;
            try {
                importe = new BigDecimal(node.path("importe_total").asText("0"));
                importe = importe.abs();
            } catch (NumberFormatException e) {
                importe = BigDecimal.ZERO;
            }
            String moneda   = node.path("moneda").asText("EUR");
            String resumen  = node.path("resumen").asText("");
            int confianza   = node.path("confianza").asInt(0);
            return TicketExtraction.ok(fecha, comercio, importe, moneda, resumen, confianza);
        } catch (Exception e) {
            log.warn("No se pudo parsear la respuesta del modelo: {}", raw, e);
            throw new VisionException("El modelo devolvió un formato inesperado");
        }
    }

    private LocalDate tryParseDate(String s) {
        try {
            return LocalDate.parse(s);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String normalizeMime(String mt) {
        if (mt == null) return "image/jpeg";
        return switch (mt.toLowerCase()) {
            case "image/jpg", "image/jpeg" -> "image/jpeg";
            case "image/png"               -> "image/png";
            case "image/webp"              -> "image/webp";
            case "image/gif"               -> "image/gif";
            default -> "image/jpeg";
        };
    }

    // ============================================================
    // DTOs
    // ============================================================

    /** Resultado de la extracción. Un solo tipo con dos formas (ok / error). */
    public record TicketExtraction(
            boolean success,
            String errorMessage,
            LocalDate fecha,
            String comercio,
            BigDecimal importe,
            String moneda,
            String resumen,
            int confianza
    ) {
        public static TicketExtraction ok(LocalDate f, String c, BigDecimal i,
                                          String m, String r, int conf) {
            return new TicketExtraction(true, null, f, c, i, m, r, conf);
        }
        public static TicketExtraction error(String msg) {
            return new TicketExtraction(false, msg, null, null, null, null, null, 0);
        }
    }

    public static class VisionException extends RuntimeException {
        public VisionException(String msg) { super(msg); }
    }
}
