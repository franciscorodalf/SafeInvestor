package es.franciscorodalf.safeinvestor.tickets.web;

import es.franciscorodalf.safeinvestor.config.Toasts;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import es.franciscorodalf.safeinvestor.movimientos.service.CsvImportService;
import es.franciscorodalf.safeinvestor.movimientos.service.MovimientoService;
import es.franciscorodalf.safeinvestor.tickets.service.TicketVisionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Flujo:
 *   GET  /movimientos/foto           → dropzone para subir la imagen
 *   POST /movimientos/foto           → envía la imagen a Claude Vision,
 *                                       cachea la extracción en sesión,
 *                                       renderiza preview editable
 *   POST /movimientos/foto/confirmar → crea el movimiento con los valores
 *                                       del formulario (posiblemente editados)
 */
@Controller
@RequestMapping("/movimientos/foto")
public class TicketWebController {

    private static final String SESSION_KEY = "ticketExtraction";

    private final TicketVisionService vision;
    private final MovimientoService movimientos;
    private final CategoriaService categorias;
    private final CsvImportService csvImport;
    private final CurrentUser currentUser;
    private final MessageSource messages;

    public TicketWebController(TicketVisionService vision,
                               MovimientoService movimientos,
                               CategoriaService categorias,
                               CsvImportService csvImport,
                               CurrentUser currentUser,
                               MessageSource messages) {
        this.vision = vision;
        this.movimientos = movimientos;
        this.categorias = categorias;
        this.csvImport = csvImport;
        this.currentUser = currentUser;
        this.messages = messages;
    }

    private String t(String key, Object... args) {
        return messages.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("configured", vision.isConfigured());
        return "movimientos/foto-form";
    }

    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file,
                         HttpSession session,
                         Model model,
                         RedirectAttributes ra) throws Exception {
        if (!vision.isConfigured()) {
            Toasts.error(ra, t("foto.not_configured"));
            return "redirect:/movimientos/foto";
        }
        if (file == null || file.isEmpty()) {
            model.addAttribute("configured", true);
            model.addAttribute("error", t("foto.err.no_file"));
            return "movimientos/foto-form";
        }
        try {
            var extraction = vision.extract(file.getBytes(), file.getContentType());
            if (!extraction.success()) {
                model.addAttribute("configured", true);
                model.addAttribute("error",
                        t("foto.err.not_ticket", extraction.errorMessage()));
                return "movimientos/foto-form";
            }
            session.setAttribute(SESSION_KEY, extraction);

            var u = currentUser.get();
            var userCats = categorias.findAll(u);
            Long sugerida = csvImport.suggestCategoryId(
                    (extraction.comercio() + " " + extraction.resumen()).trim(),
                    userCats);

            model.addAttribute("extraction", extraction);
            model.addAttribute("categorias", userCats);
            model.addAttribute("categoriaSugerida", sugerida);
            return "movimientos/foto-preview";
        } catch (Exception ex) {
            model.addAttribute("configured", true);
            model.addAttribute("error", t("foto.err.extract", ex.getMessage()));
            return "movimientos/foto-form";
        }
    }

    @PostMapping("/confirmar")
    public String confirm(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                          @RequestParam TipoMovimiento tipo,
                          @RequestParam BigDecimal importe,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam(required = false) Long categoriaId,
                          HttpSession session,
                          RedirectAttributes ra) {
        try {
            movimientos.create(currentUser.get(), categoriaId, tipo, importe, descripcion, fecha);
            session.removeAttribute(SESSION_KEY);
            Toasts.success(ra, t("foto.ok"));
            return "redirect:/movimientos";
        } catch (Exception ex) {
            Toasts.error(ra, t("foto.err.confirm", ex.getMessage()));
            return "redirect:/movimientos/foto";
        }
    }
}
