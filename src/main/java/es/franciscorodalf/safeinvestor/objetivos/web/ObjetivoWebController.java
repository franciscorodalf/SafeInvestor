package es.franciscorodalf.safeinvestor.objetivos.web;

import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.objetivos.domain.Objetivo;
import es.franciscorodalf.safeinvestor.objetivos.service.ObjetivoService;
import es.franciscorodalf.safeinvestor.objetivos.web.form.ObjetivoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/objetivos")
public class ObjetivoWebController {

    private final ObjetivoService service;
    private final CurrentUser currentUser;

    public ObjetivoWebController(ObjetivoService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("objetivos", service.findAll(currentUser.get()));
        return "objetivos/list";
    }

    @GetMapping("/nuevo")
    public String nuevoPage(Model model) {
        model.addAttribute("form", new ObjetivoForm());
        return "objetivos/form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") ObjetivoForm form,
                        BindingResult binding) {
        if (binding.hasErrors()) return "objetivos/form";
        service.create(currentUser.get(), form.getNombre(), form.getImporteObjetivo(),
            form.getFechaLimite(), form.getColor());
        return "redirect:/objetivos";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Objetivo o = service.get(currentUser.get(), id);
        model.addAttribute("objetivo", o);
        return "objetivos/detalle";
    }

    @GetMapping("/{id}/editar")
    public String editarPage(@PathVariable Long id, Model model) {
        Objetivo o = service.get(currentUser.get(), id);
        ObjetivoForm form = new ObjetivoForm();
        form.setId(o.getId());
        form.setNombre(o.getNombre());
        form.setImporteObjetivo(o.getImporteObjetivo());
        form.setFechaLimite(o.getFechaLimite());
        form.setColor(o.getColor());
        model.addAttribute("form", form);
        return "objetivos/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("form") ObjetivoForm form,
                             BindingResult binding) {
        if (binding.hasErrors()) return "objetivos/form";
        service.update(currentUser.get(), id, form.getNombre(), form.getImporteObjetivo(),
            form.getFechaLimite(), form.getColor());
        return "redirect:/objetivos/" + id;
    }

    @PostMapping("/{id}/aportar")
    public String aportar(@PathVariable Long id, @RequestParam BigDecimal importe) {
        service.aportar(currentUser.get(), id, importe);
        return "redirect:/objetivos/" + id;
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
        return "redirect:/objetivos";
    }
}
