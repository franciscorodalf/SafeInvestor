package es.franciscorodalf.safeinvestor.tickets.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Proveedor de visión sobre la API de Google Gemini (generateContent).
 *
 * Los modelos Flash tienen free tier con visión incluida (10 req/min,
 * 1.500 req/día) — más que de sobra para uso personal. API key gratuita
 * y sin tarjeta en https://aistudio.google.com.
 *
 * La key viaja en la cabecera {@code x-goog-api-key} (nunca en la URL).
 */
@Component
public class GeminiVisionProvider implements VisionProvider {

    private final GeminiProperties props;
    private final RestClient http;

    public GeminiVisionProvider(GeminiProperties props) {
        this.props = props;
        this.http = props.isConfigured()
                ? RestClient.builder().baseUrl(props.baseUrl()).build()
                : null;
    }

    @Override
    public String id() {
        return "gemini";
    }

    @Override
    public boolean isConfigured() {
        return props.isConfigured() && http != null;
    }

    @Override
    public String analyze(byte[] imageBytes, String mimeType, String prompt) {
        String base64 = Base64.getEncoder().encodeToString(imageBytes);

        Map<String, Object> req = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", base64)),
                                Map.of("text", prompt))
                )),
                "generationConfig", Map.of("maxOutputTokens", 512)
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> res = http.post()
                .uri("/models/{model}:generateContent", props.model())
                .header("x-goog-api-key", props.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .body(Map.class);

        if (res == null) {
            throw new TicketVisionService.VisionException("Respuesta vacía de Gemini");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) res.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new TicketVisionService.VisionException("Gemini no devolvió candidatos");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) {
            throw new TicketVisionService.VisionException("Gemini no devolvió contenido");
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            throw new TicketVisionService.VisionException("Gemini no devolvió texto");
        }
        return String.valueOf(parts.get(0).get("text"));
    }
}
