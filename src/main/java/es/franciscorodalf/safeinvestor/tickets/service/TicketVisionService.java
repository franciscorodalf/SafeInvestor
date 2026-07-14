package es.franciscorodalf.safeinvestor.tickets.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracción de datos estructurados de un ticket a partir de su foto.
 *
 * El trabajo pesado (llamada HTTP al modelo multimodal) se delega en un
 * {@link VisionProvider}. La selección del proveedor se controla con la
 * propiedad {@code vision.provider}:
 *
 *   - {@code auto} (default): el primero configurado, con preferencia por
 *     Gemini (free tier) sobre Anthropic (de pago).
 *   - {@code gemini} | {@code anthropic}: fuerza uno concreto.
 *
 * Este servicio es dueño del prompt y del parseo del JSON de respuesta,
 * que son idénticos para todos los proveedores — así añadir un proveedor
 * nuevo (OpenAI, Mistral...) es solo implementar la interfaz.
 */
@Service
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

    /** Orden de preferencia en modo auto: primero los gratuitos. */
    private static final List<String> AUTO_PREFERENCE = List.of("gemini", "anthropic");

    private final List<VisionProvider> providers;
    private final String configuredProvider;
    private final long maxImageBytes;
    private final ObjectMapper json = new ObjectMapper();

    public TicketVisionService(List<VisionProvider> providers,
                               @Value("${vision.provider:auto}") String configuredProvider,
                               @Value("${vision.max-image-bytes:4194304}") long maxImageBytes) {
        this.providers = providers;
        this.configuredProvider = configuredProvider;
        this.maxImageBytes = maxImageBytes;
    }

    /** El proveedor activo, o empty si ninguno está configurado. */
    public Optional<VisionProvider> activeProvider() {
        if (!"auto".equalsIgnoreCase(configuredProvider)) {
            return providers.stream()
                    .filter(p -> p.id().equalsIgnoreCase(configuredProvider) && p.isConfigured())
                    .findFirst();
        }
        return providers.stream()
                .filter(VisionProvider::isConfigured)
                .min(Comparator.comparingInt(p -> {
                    int idx = AUTO_PREFERENCE.indexOf(p.id());
                    return idx >= 0 ? idx : Integer.MAX_VALUE;
                }));
    }

    public boolean isConfigured() {
        return activeProvider().isPresent();
    }

    /** Nombre del proveedor activo para mostrar en la UI ("gemini"/"anthropic"), o null. */
    public String providerName() {
        return activeProvider().map(VisionProvider::id).orElse(null);
    }

    /** Extrae los datos del ticket. */
    public TicketExtraction extract(byte[] imageBytes, String mimeType) {
        VisionProvider provider = activeProvider()
                .orElseThrow(() -> new IllegalStateException("Ningún proveedor de visión está configurado"));
        if (imageBytes.length > maxImageBytes) {
            throw new IllegalArgumentException("La imagen es demasiado grande");
        }
        String raw = provider.analyze(imageBytes, normalizeMime(mimeType), PROMPT);
        log.debug("Respuesta de {}: {}", provider.id(), raw);
        return parseJson(raw);
    }

    /** Extrae el JSON válido dentro de la respuesta (tolerante a texto rodeando). */
    private TicketExtraction parseJson(String raw) {
        String jsonText = raw == null ? "" : raw.trim();
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
