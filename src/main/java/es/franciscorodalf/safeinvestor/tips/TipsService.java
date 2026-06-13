package es.franciscorodalf.safeinvestor.tips;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Carga el JSON de tips una vez en startup y los expone con filtros básicos.
 */
@Service
public class TipsService {

    private final ObjectMapper objectMapper;
    private List<Tip> tips = List.of();
    private List<String> categorias = List.of();

    /** Mapa de categorías a (icono Phosphor, etiqueta humana). */
    private static final Map<String, String[]> META = Map.of(
            "ahorro",      new String[]{"ph-piggy-bank",      "Ahorro"},
            "inversion",   new String[]{"ph-chart-line-up",   "Inversión"},
            "deuda",       new String[]{"ph-credit-card",     "Deuda"},
            "presupuesto", new String[]{"ph-calculator",      "Presupuesto"},
            "habito",      new String[]{"ph-target",          "Hábitos"}
    );

    public TipsService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void load() throws Exception {
        try (InputStream in = new ClassPathResource("data/tips.json").getInputStream()) {
            Tip[] arr = objectMapper.readValue(in, Tip[].class);
            this.tips = List.of(arr);
        }
        this.categorias = tips.stream()
                .map(Tip::categoria)
                .distinct()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Tip> all() {
        return tips;
    }

    public List<Tip> byCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) return tips;
        return tips.stream()
                .filter(t -> t.categoria().equalsIgnoreCase(categoria))
                .toList();
    }

    public List<String> categorias() {
        return categorias;
    }

    public String iconoDe(String categoria) {
        String[] m = META.get(categoria);
        return m != null ? m[0] : "ph-lightbulb";
    }

    public String etiquetaDe(String categoria) {
        String[] m = META.get(categoria);
        return m != null ? m[1] : categoria;
    }

    public int total() {
        return tips.size();
    }
}
