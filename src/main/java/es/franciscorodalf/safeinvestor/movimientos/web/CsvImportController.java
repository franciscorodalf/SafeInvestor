package es.franciscorodalf.safeinvestor.movimientos.web;

import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import es.franciscorodalf.safeinvestor.movimientos.service.CsvImportService;
import es.franciscorodalf.safeinvestor.movimientos.service.MovimientoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Importación masiva de movimientos desde CSV bancario.
 *
 * Flujo:
 *   GET  /movimientos/import          — form de subida
 *   POST /movimientos/import          — parsea y muestra preview editable
 *   POST /movimientos/import/confirm  — guarda en BBDD
 *
 * Las filas parseadas se cachean en sesión entre preview y confirm para
 * no volver a subir el archivo. El usuario puede editar campos y descartar
 * filas en el preview marcando un checkbox.
 */
@Controller
@RequestMapping("/movimientos/import")
public class CsvImportController {

    /** Clave en sesión donde se cachea el resultado del parseo. */
    private static final String SESSION_KEY = "csvImportRows";

    private final CsvImportService importService;
    private final MovimientoService movimientos;
    private final CategoriaService categorias;
    private final CurrentUser currentUser;

    public CsvImportController(CsvImportService importService,
                               MovimientoService movimientos,
                               CategoriaService categorias,
                               CurrentUser currentUser) {
        this.importService = importService;
        this.movimientos = movimientos;
        this.categorias = categorias;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String form() {
        return "movimientos/import-form";
    }

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file,
                         HttpSession session,
                         Model model) throws Exception {
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "Selecciona un archivo CSV.");
            return "movimientos/import-form";
        }
        var u = currentUser.get();
        CsvImportService.ParseResult result = importService.parse(u, file.getBytes());

        if (result.rows.isEmpty()) {
            model.addAttribute("error",
                    "No se ha podido reconocer ninguna fila del CSV. " +
                    "Comprueba que tenga columnas de fecha, importe y descripción.");
            return "movimientos/import-form";
        }

        session.setAttribute(SESSION_KEY, result.rows);
        model.addAttribute("rows", result.rows);
        model.addAttribute("separator", String.valueOf(result.separator));
        model.addAttribute("skipped", result.skippedLines);
        model.addAttribute("categorias", categorias.findAll(u));
        return "movimientos/import-preview";
    }

    @PostMapping("/confirm")
    @SuppressWarnings("unchecked")
    public String confirm(@RequestParam(value = "keep", required = false) List<Integer> keep,
                          @RequestParam("fecha") List<String> fechas,
                          @RequestParam("tipo") List<String> tipos,
                          @RequestParam("importe") List<String> importes,
                          @RequestParam("descripcion") List<String> descripciones,
                          @RequestParam("categoriaId") List<String> categoriaIds,
                          HttpSession session,
                          Model model) {
        var u = currentUser.get();
        List<CsvImportService.Row> stored = (List<CsvImportService.Row>) session.getAttribute(SESSION_KEY);
        if (stored == null) {
            return "redirect:/movimientos/import";
        }

        List<Integer> kept = keep == null ? List.of() : keep;
        int created = 0;
        List<String> errors = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        for (int i = 0; i < stored.size(); i++) {
            if (!kept.contains(i)) continue;
            try {
                LocalDate fecha = LocalDate.parse(fechas.get(i), fmt);
                TipoMovimiento tipo = TipoMovimiento.valueOf(tipos.get(i));
                BigDecimal importe = new BigDecimal(importes.get(i).replace(',', '.'));
                String desc = descripciones.get(i);
                Long catId = parseCategoriaId(categoriaIds.get(i));
                movimientos.create(u, catId, tipo, importe, desc, fecha);
                created++;
            } catch (Exception ex) {
                errors.add("Fila " + (i + 1) + ": " + ex.getMessage());
            }
        }

        session.removeAttribute(SESSION_KEY);
        model.addAttribute("created", created);
        model.addAttribute("errors", errors);
        return "movimientos/import-done";
    }

    private static Long parseCategoriaId(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }
}
