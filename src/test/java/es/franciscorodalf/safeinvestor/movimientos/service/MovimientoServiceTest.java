package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MovimientoServiceTest {

    @Autowired UsuarioService usuarioService;
    @Autowired CategoriaService categoriaService;
    @Autowired MovimientoService service;

    @Test
    void creaYBuscaMovimientos() {
        Usuario u = usuarioService.register("mov-test@x.com", "Test", "password123");
        var comida = categoriaService.findAll(u).stream()
            .filter(c -> c.getNombre().equals("Comida")).findFirst().orElseThrow();

        service.create(u, comida.getId(), TipoMovimiento.GASTO,
            new BigDecimal("12.50"), "Almuerzo", LocalDate.now());
        service.create(u, null, TipoMovimiento.INGRESO,
            new BigDecimal("100.00"), "Regalo", LocalDate.now());

        var page = service.search(u, null, null, null, 0);
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void noPermiteAccederAMovimientoDeOtroUsuario() {
        Usuario u1 = usuarioService.register("u1@x.com", "U1", "password123");
        Usuario u2 = usuarioService.register("u2@x.com", "U2", "password123");
        var m = service.create(u1, null, TipoMovimiento.GASTO,
            new BigDecimal("5.00"), null, LocalDate.now());
        assertThrows(MovimientoService.MovimientoNotFoundException.class,
            () -> service.get(u2, m.getId()));
    }
}
