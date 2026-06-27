package es.franciscorodalf.safeinvestor.auth.web;

import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Pantalla de perfil: cambiar nombre, cambiar contraseña, borrar cuenta.
 * Todas las acciones invalidan la sesión cuando es necesario (borrado).
 */
@Controller
@RequestMapping("/perfil")
public class PerfilWebController {

    private final UsuarioService usuarioService;
    private final CurrentUser currentUser;

    public PerfilWebController(UsuarioService usuarioService, CurrentUser currentUser) {
        this.usuarioService = usuarioService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String perfil(Model model) {
        model.addAttribute("usuario", currentUser.get());
        return "perfil";
    }

    @PostMapping("/nombre")
    public String updateNombre(@RequestParam String nombre, Model model) {
        try {
            usuarioService.updateNombre(currentUser.get(), nombre);
            return "redirect:/perfil?ok=nombre";
        } catch (IllegalArgumentException ex) {
            return "redirect:/perfil?err=nombre";
        }
    }

    @PostMapping("/password")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword) {
        try {
            usuarioService.changePasswordVerifying(currentUser.get(), currentPassword, newPassword);
            return "redirect:/perfil?ok=password";
        } catch (UsuarioService.InvalidCurrentPasswordException ex) {
            return "redirect:/perfil?err=current";
        } catch (IllegalArgumentException ex) {
            return "redirect:/perfil?err=newshort";
        }
    }

    @PostMapping("/borrar")
    public String deleteAccount(@RequestParam String emailConfirm,
                                HttpServletRequest request) {
        try {
            usuarioService.deleteAccount(currentUser.get(), emailConfirm);
        } catch (IllegalArgumentException ex) {
            return "redirect:/perfil?err=email";
        }
        // Invalida la sesión actual para que no quede colgada
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/?cuenta_borrada";
    }
}
