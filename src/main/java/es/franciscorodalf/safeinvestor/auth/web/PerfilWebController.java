package es.franciscorodalf.safeinvestor.auth.web;

import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import es.franciscorodalf.safeinvestor.config.Toasts;
import es.franciscorodalf.safeinvestor.movimientos.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantalla de perfil: cambiar nombre, cambiar contraseña, borrar cuenta.
 * Todas las acciones invalidan la sesión cuando es necesario (borrado).
 */
@Controller
@RequestMapping("/perfil")
public class PerfilWebController {

    private final UsuarioService usuarioService;
    private final CurrentUser currentUser;
    private final MessageSource messages;

    public PerfilWebController(UsuarioService usuarioService,
                               CurrentUser currentUser,
                               MessageSource messages) {
        this.usuarioService = usuarioService;
        this.currentUser = currentUser;
        this.messages = messages;
    }

    private String t(String key) {
        return messages.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    @GetMapping
    public String perfil(Model model) {
        model.addAttribute("usuario", currentUser.get());
        return "perfil";
    }

    @PostMapping("/nombre")
    public String updateNombre(@RequestParam String nombre, RedirectAttributes ra) {
        try {
            usuarioService.updateNombre(currentUser.get(), nombre);
            Toasts.success(ra, t("perfil.flash.nombre_ok"));
        } catch (IllegalArgumentException ex) {
            Toasts.error(ra, t("perfil.flash.nombre_err"));
        }
        return "redirect:/perfil";
    }

    @PostMapping("/password")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 RedirectAttributes ra) {
        try {
            usuarioService.changePasswordVerifying(currentUser.get(), currentPassword, newPassword);
            Toasts.success(ra, t("perfil.flash.password_ok"));
        } catch (UsuarioService.InvalidCurrentPasswordException ex) {
            Toasts.error(ra, t("perfil.flash.current_err"));
        } catch (IllegalArgumentException ex) {
            Toasts.error(ra, t("perfil.flash.newshort_err"));
        }
        return "redirect:/perfil";
    }

    @PostMapping("/borrar")
    public String deleteAccount(@RequestParam String emailConfirm,
                                HttpServletRequest request,
                                RedirectAttributes ra) {
        try {
            usuarioService.deleteAccount(currentUser.get(), emailConfirm);
        } catch (IllegalArgumentException ex) {
            Toasts.error(ra, t("perfil.flash.email_err"));
            return "redirect:/perfil";
        }
        // Invalida la sesión actual para que no quede colgada
        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }
        SecurityContextHolder.clearContext();
        return "redirect:/";
    }
}
