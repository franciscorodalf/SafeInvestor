package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import es.franciscorodalf.safeinvestor.movimientos.domain.CategoriaRepository;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Parser de CSV bancario tolerante a múltiples formatos.
 *
 * Auto-detecta:
 *   - Separador: ',', ';' o tabulador.
 *   - Decimal: coma (Europa) o punto (US/UK).
 *   - Fecha: dd/MM/yyyy, dd-MM-yyyy, yyyy-MM-dd, dd.MM.yyyy.
 *   - Tipo: por signo del importe (negativo = GASTO, positivo = INGRESO).
 *
 * Para cada fila intenta detectar columnas por su contenido (date-like,
 * number-like, text-like) sin requerir un orden fijo. La cabecera, si
 * existe, se ignora.
 *
 * Auto-sugerencia de categoría por matching de keywords sobre el campo
 * descripción contra los nombres de categoría existentes del usuario.
 */
@Service
public class CsvImportService {

    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    /** Mapa de keywords → nombres canónicos de categoría (matching contra cat del usuario, case-insensitive). */
    private static final Map<String, List<String>> KEYWORDS = Map.ofEntries(
            Map.entry("Alimentación",   List.of("mercadona", "carrefour", "lidl", "alcampo", "dia ", "consum", "supermercado", "alimentacion", "frutería")),
            Map.entry("Transporte",     List.of("renfe", "metro", "uber", "cabify", "bizum taxi", "taxi", "gasolina", "repsol", "cepsa", "shell", "bp ", "parking")),
            Map.entry("Ocio",           List.of("netflix", "spotify", "hbo", "disney", "cinesa", "yelmo", "kinepolis", "concierto", "teatro", "videojuego", "steam")),
            Map.entry("Restaurantes",   List.of("restaurante", "bar ", "mcdonalds", "burger", "kfc", "telepizza", "dominos", "starbucks", "cafetería", "glovo", "uber eats", "just eat")),
            Map.entry("Hogar",          List.of("alquiler", "hipoteca", "comunidad", "luz", "endesa", "iberdrola", "naturgy", "agua", "gas natural", "ikea", "leroy")),
            Map.entry("Salud",          List.of("farmacia", "clinica", "dentista", "óptica", "gimnasio", "fitness")),
            Map.entry("Telefonía",      List.of("movistar", "vodafone", "orange", "yoigo", "masmovil", "tarifa móvil")),
            Map.entry("Compras",        List.of("amazon", "aliexpress", "shein", "zara", "h&m", "el corte ingles", "decathlon")),
            Map.entry("Salario",        List.of("nomina", "nómina", "salario", "transferencia recibida")),
            Map.entry("Suscripciones",  List.of("apple.com", "google play", "icloud", "adobe", "office", "github", "domain", "hosting"))
    );

    private final CategoriaRepository categorias;

    public CsvImportService(CategoriaRepository categorias) {
        this.categorias = categorias;
    }

    /** Resultado de un parseo. */
    public static final class ParseResult {
        public final List<Row> rows;
        public final char separator;
        public final int skippedLines;

        public ParseResult(List<Row> rows, char separator, int skippedLines) {
            this.rows = rows;
            this.separator = separator;
            this.skippedLines = skippedLines;
        }
    }

    /** Fila parseada lista para ser editada y guardada. */
    public static final class Row {
        public LocalDate fecha;
        public TipoMovimiento tipo;
        public BigDecimal importe;
        public String descripcion;
        public Long categoriaIdSugerida; // null si no se ha podido sugerir

        public LocalDate getFecha() { return fecha; }
        public TipoMovimiento getTipo() { return tipo; }
        public BigDecimal getImporte() { return importe; }
        public String getDescripcion() { return descripcion; }
        public Long getCategoriaIdSugerida() { return categoriaIdSugerida; }
    }

    public ParseResult parse(Usuario usuario, byte[] csvBytes) {
        Charset charset = detectCharset(csvBytes);
        String raw = new String(csvBytes, charset);
        // Retirar BOM si está
        if (!raw.isEmpty() && raw.charAt(0) == '﻿') raw = raw.substring(1);

        String[] lines = raw.split("\\r?\\n");
        char separator = detectSeparator(lines);

        List<Categoria> userCats = categorias.findByUsuarioOrderByNombre(usuario);
        List<Row> rows = new ArrayList<>();
        int skipped = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) { skipped++; continue; }
            String[] cells = splitRespectingQuotes(line, separator);
            Row row = tryParseRow(cells, userCats);
            if (row == null) {
                skipped++;
                log.debug("Línea {} ignorada (no se pudo parsear): {}", i + 1, line);
                continue;
            }
            rows.add(row);
        }
        return new ParseResult(rows, separator, skipped);
    }

    // === Detección de separador ===
    private char detectSeparator(String[] lines) {
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : new char[]{',', ';', '\t', '|'}) counts.put(c, 0);
        int probes = Math.min(lines.length, 8);
        for (int i = 0; i < probes; i++) {
            String l = lines[i];
            for (Character c : counts.keySet()) {
                counts.merge(c, (int) l.chars().filter(ch -> ch == c).count(), Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(',');
    }

    // === Detección de encoding ===
    private Charset detectCharset(byte[] bytes) {
        // BOM UTF-8: EF BB BF
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            return StandardCharsets.UTF_8;
        }
        // Heurística: si decodificando como UTF-8 da error o salen muchos ?,
        // caemos a ISO-8859-1 (típico exports bancarios españoles).
        String utf8 = new String(bytes, StandardCharsets.UTF_8);
        if (utf8.contains("�")) return Charset.forName("ISO-8859-1");
        return StandardCharsets.UTF_8;
    }

    // === Split que respeta comillas ===
    private String[] splitRespectingQuotes(String line, char sep) {
        List<String> result = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == sep && !inQuotes) {
                result.add(buf.toString());
                buf.setLength(0);
            } else {
                buf.append(c);
            }
        }
        result.add(buf.toString());
        return result.toArray(new String[0]);
    }

    // === Parseo de una fila ===
    private Row tryParseRow(String[] cells, List<Categoria> userCats) {
        LocalDate fecha = null;
        BigDecimal importe = null;
        String descripcion = null;
        StringBuilder descBuilder = new StringBuilder();

        for (String raw : cells) {
            String cell = raw == null ? "" : raw.trim().replaceAll("^\"|\"$", "");
            if (cell.isEmpty()) continue;

            if (fecha == null) {
                LocalDate d = tryDate(cell);
                if (d != null) { fecha = d; continue; }
            }
            if (importe == null) {
                BigDecimal n = tryNumber(cell);
                if (n != null) { importe = n; continue; }
            }
            // Cualquier celda no-fecha no-número la consideramos parte de descripción
            if (!descBuilder.isEmpty()) descBuilder.append(" · ");
            descBuilder.append(cell);
        }

        if (fecha == null || importe == null) return null;
        descripcion = descBuilder.isEmpty() ? null : descBuilder.toString();
        if (descripcion != null && descripcion.length() > 200) {
            descripcion = descripcion.substring(0, 200);
        }

        Row row = new Row();
        row.fecha = fecha;
        row.tipo = importe.signum() < 0 ? TipoMovimiento.GASTO : TipoMovimiento.INGRESO;
        row.importe = importe.abs();
        row.descripcion = descripcion;
        row.categoriaIdSugerida = sugerirCategoria(descripcion, userCats);
        return row;
    }

    private LocalDate tryDate(String s) {
        for (DateTimeFormatter f : DATE_FORMATS) {
            try { return LocalDate.parse(s, f); } catch (Exception ignored) {}
        }
        return null;
    }

    private BigDecimal tryNumber(String s) {
        // Una cifra puede ser \"-1.234,56\" o \"-1,234.56\" o \"1234.56\" o \"-50\".
        // Heurística: si hay coma Y punto, el último carácter define el decimal.
        // Si solo hay coma → decimal coma. Si solo hay punto → decimal punto.
        String t = s.replace(" ", "").replace(" ", "").replace("€", "").replace("$", "").replace("£", "");
        if (t.isEmpty()) return null;
        boolean hasComma = t.contains(",");
        boolean hasDot = t.contains(".");
        String normalized;
        if (hasComma && hasDot) {
            int lastComma = t.lastIndexOf(',');
            int lastDot = t.lastIndexOf('.');
            if (lastComma > lastDot) {
                // coma decimal, puntos miles
                normalized = t.replace(".", "").replace(",", ".");
            } else {
                normalized = t.replace(",", "");
            }
        } else if (hasComma) {
            normalized = t.replace(".", "").replace(",", ".");
        } else {
            normalized = t;
        }
        try {
            BigDecimal n = new BigDecimal(normalized);
            // Filtrar números que claramente no son importes (e.g. años: 2024)
            // Solo si el string original no contenía decimal y el valor coincide con un año razonable.
            if (!hasComma && !hasDot) {
                int asInt = n.intValue();
                if (asInt >= 1900 && asInt <= 2100) return null;
            }
            return n;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long sugerirCategoria(String descripcion, List<Categoria> userCats) {
        if (descripcion == null || userCats.isEmpty()) return null;
        String lower = descripcion.toLowerCase();
        // Match exacto contra alguna keyword del mapa
        for (Map.Entry<String, List<String>> entry : KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                if (lower.contains(kw)) {
                    // Buscar categoría del usuario cuyo nombre case (ignore case + accent-tolerant)
                    String canon = entry.getKey().toLowerCase();
                    for (Categoria c : userCats) {
                        if (normaliza(c.getNombre()).contains(normaliza(canon))
                                || normaliza(canon).contains(normaliza(c.getNombre()))) {
                            return c.getId();
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String normaliza(String s) {
        return s == null ? "" : s.toLowerCase()
                .replace('á','a').replace('é','e').replace('í','i').replace('ó','o').replace('ú','u')
                .replace('ñ','n');
    }
}
