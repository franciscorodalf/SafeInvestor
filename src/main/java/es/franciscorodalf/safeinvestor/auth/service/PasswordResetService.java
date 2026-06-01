package es.franciscorodalf.safeinvestor.auth.service;

import es.franciscorodalf.safeinvestor.auth.domain.PasswordResetToken;
import es.franciscorodalf.safeinvestor.auth.domain.PasswordResetTokenRepository;
import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.domain.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final int VALID_HOURS = 1;

    private final UsuarioRepository usuarios;
    private final PasswordResetTokenRepository tokens;
    private final UsuarioService usuarioService;
    private final String baseUrl;

    public PasswordResetService(UsuarioRepository usuarios,
                                PasswordResetTokenRepository tokens,
                                UsuarioService usuarioService,
                                @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.usuarios = usuarios;
        this.tokens = tokens;
        this.usuarioService = usuarioService;
        this.baseUrl = baseUrl;
    }

    /** Crea un token y lo registra. Loguea el link de reset en consola (en dev). */
    @Transactional
    public void requestReset(String email) {
        Optional<Usuario> maybeUser = usuarios.findByEmailIgnoreCase(email);
        if (maybeUser.isEmpty()) {
            log.info("Reset solicitado para email no registrado: {}", email);
            return;
        }
        byte[] raw = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        OffsetDateTime expires = OffsetDateTime.now().plusHours(VALID_HOURS);
        tokens.save(new PasswordResetToken(maybeUser.get(), token, expires));
        log.info("=== RESET PASSWORD LINK (dev) === {}/reset/{}", baseUrl, token);
    }

    @Transactional
    public void consumeReset(String token, String newPassword) {
        PasswordResetToken prt = tokens.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException());
        OffsetDateTime now = OffsetDateTime.now();
        if (!prt.isUsable(now)) {
            throw new InvalidTokenException();
        }
        usuarioService.changePassword(prt.getUsuario(), newPassword);
        prt.markUsed(now);
        tokens.save(prt);
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException() { super("Token de reset inválido o expirado"); }
    }
}
