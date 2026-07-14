package es.franciscorodalf.safeinvestor.tickets.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Proveedor de visión sobre la Messages API de Anthropic (Claude).
 * De pago (por tokens), pero con mejor precisión en tickets difíciles.
 * Se activa con ANTHROPIC_API_KEY + VISION_PROVIDER=anthropic (o si no
 * hay ninguna GEMINI_API_KEY configurada en modo auto).
 */
@Component
public class AnthropicVisionProvider implements VisionProvider {

    private final AnthropicProperties props;
    private final RestClient http;

    public AnthropicVisionProvider(AnthropicProperties props) {
        this.props = props;
        this.http = props.isConfigured()
                ? RestClient.builder().baseUrl(props.baseUrl()).build()
                : null;
    }

    @Override
    public String id() {
        return "anthropic";
    }

    @Override
    public boolean isConfigured() {
        return props.isConfigured() && http != null;
    }

    @Override
    public String analyze(byte[] imageBytes, String mimeType, String prompt) {
        String base64 = Base64.getEncoder().encodeToString(imageBytes);

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
                                                "media_type", mimeType,
                                                "data", base64)),
                                Map.of("type", "text", "text", prompt))
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

        if (res == null) {
            throw new TicketVisionService.VisionException("Respuesta vacía de Anthropic");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) res.get("content");
        if (content == null || content.isEmpty()) {
            throw new TicketVisionService.VisionException("Anthropic no devolvió contenido");
        }
        return String.valueOf(content.get(0).get("text"));
    }
}
