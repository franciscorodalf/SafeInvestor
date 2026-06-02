package es.franciscorodalf.safeinvestor.auth.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.domain.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UsuarioServiceTest {

    @Autowired private UsuarioService service;
    @Autowired private UsuarioRepository repo;
    @Autowired private PasswordEncoder encoder;

    @BeforeEach
    void clean() { repo.deleteAll(); }

    @Test
    void registersNewUserWithHashedPassword() {
        Usuario u = service.register("a@b.com", "Alice", "password123");
        assertNotNull(u.getId());
        assertEquals("a@b.com", u.getEmail());
        assertTrue(encoder.matches("password123", u.getPasswordHash()));
    }

    @Test
    void rejectsDuplicateEmail() {
        service.register("a@b.com", "Alice", "password123");
        assertThrows(UsuarioService.EmailAlreadyRegisteredException.class,
            () -> service.register("A@B.COM", "Other", "password456"));
    }
}
