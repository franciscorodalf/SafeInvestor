package es.franciscorodalf.safeinvestor.auth.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.domain.UsuarioRepository;
import es.franciscorodalf.safeinvestor.movimientos.service.CategoriaService;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final CategoriaService categoriaService;

    public UsuarioService(UsuarioRepository usuarios,
                          PasswordEncoder passwordEncoder,
                          @Lazy CategoriaService categoriaService) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.categoriaService = categoriaService;
    }

    @Transactional
    public Usuario register(String email, String nombre, String rawPassword) {
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        Usuario u = new Usuario(email.toLowerCase(), nombre, passwordEncoder.encode(rawPassword));
        Usuario saved = usuarios.save(u);
        categoriaService.seedDefaults(saved);
        return saved;
    }

    @Transactional
    public void changePassword(Usuario usuario, String newRawPassword) {
        usuario.setPasswordHash(passwordEncoder.encode(newRawPassword));
        usuarios.save(usuario);
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException {
        public EmailAlreadyRegisteredException(String email) {
            super("El email ya está registrado: " + email);
        }
    }
}
