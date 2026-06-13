package es.franciscorodalf.safeinvestor.auth.web;

import es.franciscorodalf.safeinvestor.auth.service.PasswordResetService;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import es.franciscorodalf.safeinvestor.auth.web.form.RegisterForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthWebController {

    private final UsuarioService usuarioService;
    private final PasswordResetService passwordResetService;

    public AuthWebController(UsuarioService usuarioService, PasswordResetService passwordResetService) {
        this.usuarioService = usuarioService;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            @RequestParam(required = false) String reset,
                            Model model) {
        // SIEMPRE añadir como Boolean (nunca null) para evitar el truncate de Thymeleaf
        model.addAttribute("msgError", error != null);
        model.addAttribute("msgLogout", logout != null);
        model.addAttribute("msgRegistered", registered != null);
        model.addAttribute("msgReset", reset != null);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("form") RegisterForm form,
                                 BindingResult binding,
                                 Model model) {
        if (binding.hasErrors()) {
            return "auth/register";
        }
        try {
            usuarioService.register(form.getEmail(), form.getNombre(), form.getPassword());
        } catch (UsuarioService.EmailAlreadyRegisteredException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/forgot")
    public String forgotPage() { return "auth/forgot"; }

    @PostMapping("/forgot")
    public String forgotSubmit(@RequestParam String email, Model model) {
        passwordResetService.requestReset(email);
        model.addAttribute("sent", true);
        return "auth/forgot";
    }

    @GetMapping("/reset/{token}")
    public String resetPage(@PathVariable String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset";
    }

    @PostMapping("/reset/{token}")
    public String resetSubmit(@PathVariable String token,
                              @RequestParam String password,
                              Model model) {
        try {
            passwordResetService.consumeReset(token, password);
        } catch (PasswordResetService.InvalidTokenException e) {
            model.addAttribute("token", token);
            model.addAttribute("error", e.getMessage());
            return "auth/reset";
        }
        return "redirect:/login?reset";
    }
}
