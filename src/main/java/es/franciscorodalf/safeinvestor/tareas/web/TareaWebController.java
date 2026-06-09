package es.franciscorodalf.safeinvestor.tareas.web;

import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.tareas.service.TareaService;
import es.franciscorodalf.safeinvestor.tareas.web.form.TareaForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tareas")
public class TareaWebController {

    private final TareaService service;
    private final CurrentUser currentUser;

    public TareaWebController(TareaService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tareas", service.findAll(currentUser.get()));
        model.addAttribute("form", new TareaForm());
        return "tareas/list";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") TareaForm form,
                        BindingResult binding,
                        Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("tareas", service.findAll(currentUser.get()));
            return "tareas/list";
        }
        service.create(currentUser.get(), form.getTitulo(), form.getDescripcion(), form.getFechaVencimiento());
        return "redirect:/tareas";
    }

    @PostMapping("/{id}/completar")
    public String completar(@PathVariable Long id) {
        service.completar(currentUser.get(), id);
        return "redirect:/tareas";
    }

    @PostMapping("/{id}/descompletar")
    public String descompletar(@PathVariable Long id) {
        service.descompletar(currentUser.get(), id);
        return "redirect:/tareas";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
        return "redirect:/tareas";
    }
}
