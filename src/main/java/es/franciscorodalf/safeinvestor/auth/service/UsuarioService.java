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

    /** Cambia la contraseña validando la actual. Útil desde la pantalla de perfil. */
    @Transactional
    public void changePasswordVerifying(Usuario usuario, String currentRawPassword, String newRawPassword) {
        if (!passwordEncoder.matches(currentRawPassword, usuario.getPasswordHash())) {
            throw new InvalidCurrentPasswordException();
        }
        if (newRawPassword == null || newRawPassword.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");
        }
        changePassword(usuario, newRawPassword);
    }

    @Transactional
    public Usuario updateNombre(Usuario usuario, String nombre) {
        if (nombre == null || nombre.isBlank() || nombre.length() > 100) {
            throw new IllegalArgumentException("Nombre inválido");
        }
        usuario.setNombre(nombre.trim());
        return usuarios.save(usuario);
    }

    /** Borra la cuenta y todos sus datos (CASCADE en FKs). Requiere reescribir el email como confirmación. */
    @Transactional
    public void deleteAccount(Usuario usuario, String emailConfirmation) {
        if (emailConfirmation == null || !emailConfirmation.equalsIgnoreCase(usuario.getEmail())) {
            throw new IllegalArgumentException("La confirmación del email no coincide");
        }
        usuarios.delete(usuario);
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException {
        public EmailAlreadyRegisteredException(String email) {
            super("El email ya está registrado: " + email);
        }
    }

    public static class InvalidCurrentPasswordException extends RuntimeException {
        public InvalidCurrentPasswordException() {
            super("La contraseña actual no es correcta");
        }
    }
}
