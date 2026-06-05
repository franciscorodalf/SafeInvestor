package es.franciscorodalf.safeinvestor.movimientos.security;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.domain.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UsuarioRepository usuarios;

    public CurrentUser(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        String email = auth.getName();
        return usuarios.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalStateException("Usuario autenticado no existe: " + email));
    }
}
