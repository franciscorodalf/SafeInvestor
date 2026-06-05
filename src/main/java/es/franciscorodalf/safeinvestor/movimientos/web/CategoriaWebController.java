package es.franciscorodalf.safeinvestor.movimientos.web;

import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categorias")
public class CategoriaWebController {

    private final CategoriaService service;
    private final CurrentUser currentUser;

    public CategoriaWebController(CategoriaService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categorias", service.findAll(currentUser.get()));
        return "categorias/list";
    }

    @PostMapping
    public String crear(@RequestParam String nombre,
                        @RequestParam(required = false) String color) {
        service.create(currentUser.get(), nombre, color != null ? color : "#6B7280", null);
        return "redirect:/categorias";
    }

    @PostMapping("/{id}/borrar")
    public String borrar(@PathVariable Long id) {
        service.delete(currentUser.get(), id);
        return "redirect:/categorias";
    }
}
