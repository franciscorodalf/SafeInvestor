package es.franciscorodalf.safeinvestor.presupuestos.web;

import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import es.franciscorodalf.safeinvestor.presupuestos.service.PresupuestoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/presupuestos")
public class PresupuestoWebController {

    private final PresupuestoService presupuestos;
    private final CategoriaService categorias;
    private final CurrentUser currentUser;

    public PresupuestoWebController(PresupuestoService presupuestos,
                                    CategoriaService categorias,
                                    CurrentUser currentUser) {
        this.presupuestos = presupuestos;
        this.categorias = categorias;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Integer anio,
                       @RequestParam(required = false) Integer mes,
                       Model model) {
        var u = currentUser.get();
        LocalDate hoy = LocalDate.now();
        int a = anio != null ? anio : hoy.getYear();
        int m = mes  != null ? mes  : hoy.getMonthValue();

        model.addAttribute("anio", a);
        model.addAttribute("mes", m);
        model.addAttribute("presupuestos", presupuestos.vistaDelMes(u, a, m));
        model.addAttribute("categorias", categorias.findAll(u));
        // Para navegación mes anterior/siguiente
        LocalDate ref = LocalDate.of(a, m, 1);
        model.addAttribute("mesAnterior", ref.minusMonths(1));
        model.addAttribute("mesSiguiente", ref.plusMonths(1));
        return "presupuestos/list";
    }

    @PostMapping
    public String upsert(@RequestParam Long categoriaId,
                         @RequestParam int anio,
                         @RequestParam int mes,
                         @RequestParam BigDecimal limite) {
        presupuestos.upsert(currentUser.get(), categoriaId, anio, mes, limite);
        return "redirect:/presupuestos?anio=" + anio + "&mes=" + mes;
    }

    @PostMapping("/{id}/borrar")
    public String delete(@PathVariable Long id,
                         @RequestParam int anio,
                         @RequestParam int mes) {
        presupuestos.delete(currentUser.get(), id);
        return "redirect:/presupuestos?anio=" + anio + "&mes=" + mes;
    }
}
