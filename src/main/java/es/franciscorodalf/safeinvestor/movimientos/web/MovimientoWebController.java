package es.franciscorodalf.safeinvestor.movimientos.web;

import es.franciscorodalf.safeinvestor.movimientos.domain.Movimiento;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import es.franciscorodalf.safeinvestor.movimientos.service.MovimientoService;
import es.franciscorodalf.safeinvestor.movimientos.web.form.MovimientoForm;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/movimientos")
public class MovimientoWebController {

    private final MovimientoService movimientos;
    private final CategoriaService categorias;
    private final CurrentUser currentUser;

    public MovimientoWebController(MovimientoService movimientos,
                                   CategoriaService categorias,
                                   CurrentUser currentUser) {
        this.movimientos = movimientos;
        this.categorias = categorias;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String list(
        @RequestParam(required = false) Long categoriaId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        var u = currentUser.get();
        var pageResult = movimientos.search(u, categoriaId, desde, hasta, q, page);
        model.addAttribute("movimientos", pageResult.getContent());
        model.addAttribute("categorias", categorias.findAll(u));
        model.addAttribute("currentCategoriaId", categoriaId);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("q", q);
        model.addAttribute("page", page);
        model.addAttribute("hasNext", pageResult.hasNext());
        model.addAttribute("hasPrev", pageResult.hasPrevious());
        return "movimientos/list";
    }

    @GetMapping("/nuevo")
    public String nuevoPage(Model model) {
        model.addAttribute("form", new MovimientoForm());
        model.addAttribute("categorias", categorias.findAll(currentUser.get()));
        return "movimientos/form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") MovimientoForm form,
                        BindingResult binding,
                        Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("categorias", categorias.findAll(currentUser.get()));
            return "movimientos/form";
        }
        movimientos.create(currentUser.get(), form.getCategoriaId(), form.getTipo(),
            form.getImporte(), form.getDescripcion(), form.getFecha());
        return "redirect:/movimientos";
    }

    @GetMapping("/{id}/editar")
    public String editarPage(@PathVariable Long id, Model model) {
        Movimiento m = movimientos.get(currentUser.get(), id);
        MovimientoForm form = new MovimientoForm();
        form.setId(m.getId());
        form.setCategoriaId(m.getCategoria() != null ? m.getCategoria().getId() : null);
        form.setTipo(m.getTipo());
        form.setImporte(m.getImporte());
        form.setDescripcion(m.getDescripcion());
        form.setFecha(m.getFecha());
        model.addAttribute("form", form);
        model.addAttribute("categorias", categorias.findAll(currentUser.get()));
        return "movimientos/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("form") MovimientoForm form,
                             BindingResult binding,
                             Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("categorias", categorias.findAll(currentUser.get()));
            return "movimientos/form";
        }
        movimientos.update(currentUser.get(), id, form.getCategoriaId(), form.getTipo(),
            form.getImporte(), form.getDescripcion(), form.getFecha());
        return "redirect:/movimientos";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id) {
        movimientos.delete(currentUser.get(), id);
        return "redirect:/movimientos";
    }
}
