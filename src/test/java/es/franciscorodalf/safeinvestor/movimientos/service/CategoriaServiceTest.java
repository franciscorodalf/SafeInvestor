package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoriaServiceTest {

    @Autowired UsuarioService usuarioService;
    @Autowired CategoriaService service;

    @Test
    void usuarioRegistradoTieneCategoriasDefault() {
        Usuario u = usuarioService.register("cat-test@x.com", "Test", "password123");
        var lista = service.findAll(u);
        assertEquals(11, lista.size(),
            "Cada usuario nuevo debería tener 11 categorías default");
        assertTrue(lista.stream().anyMatch(c -> c.getNombre().equals("Comida")));
        assertTrue(lista.stream().anyMatch(c -> c.getNombre().equals("Nómina")));
    }

    @Test
    void noPermiteNombresDuplicados() {
        Usuario u = usuarioService.register("dup-cat@x.com", "Dup", "password123");
        service.create(u, "Personal", "#FF0000", null);
        assertThrows(CategoriaService.CategoriaDuplicadaException.class,
            () -> service.create(u, "PERSONAL", "#00FF00", null));
    }
}
