package es.franciscorodalf.safeinvestor.auth.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.domain.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario register(String email, String nombre, String rawPassword) {
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }
        Usuario u = new Usuario(email.toLowerCase(), nombre, passwordEncoder.encode(rawPassword));
        return usuarios.save(u);
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
