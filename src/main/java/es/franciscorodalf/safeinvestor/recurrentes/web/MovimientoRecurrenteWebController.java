package es.franciscorodalf.safeinvestor.recurrentes.web;

import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import es.franciscorodalf.safeinvestor.recurrentes.domain.Frecuencia;
import es.franciscorodalf.safeinvestor.recurrentes.service.MovimientoRecurrenteService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/recurrentes")
public class MovimientoRecurrenteWebController {

    private final MovimientoRecurrenteService recurrentes;
    private final CategoriaService categorias;
    private final CurrentUser currentUser;

    public MovimientoRecurrenteWebController(MovimientoRecurrenteService recurrentes,
                                             CategoriaService categorias,
                                             CurrentUser currentUser) {
        this.recurrentes = recurrentes;
        this.categorias = categorias;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String list(Model model) {
        var u = currentUser.get();
        model.addAttribute("recurrentes", recurrentes.findAll(u));
        model.addAttribute("categorias", categorias.findAll(u));
        model.addAttribute("frecuencias", Frecuencia.values());
        return "recurrentes/list";
    }

    @PostMapping
    public String create(@RequestParam(required = false) Long categoriaId,
                         @RequestParam TipoMovimiento tipo,
                         @RequestParam BigDecimal importe,
                         @RequestParam(required = false) String descripcion,
                         @RequestParam Frecuencia frecuencia,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate proximaEjecucion) {
        recurrentes.create(currentUser.get(), categoriaId, tipo, importe,
                descripcion, frecuencia, proximaEjecucion);
        return "redirect:/recurrentes";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        recurrentes.toggleActivo(currentUser.get(), id);
        return "redirect:/recurrentes";
    }

    @PostMapping("/{id}/borrar")
    public String delete(@PathVariable Long id) {
        recurrentes.delete(currentUser.get(), id);
        return "redirect:/recurrentes";
    }
}
