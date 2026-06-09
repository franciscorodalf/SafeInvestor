package es.franciscorodalf.safeinvestor.objetivos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import es.franciscorodalf.safeinvestor.objetivos.domain.Objetivo;
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
class ObjetivoServiceTest {

    @Autowired UsuarioService usuarioService;
    @Autowired ObjetivoService service;

    @Test
    void aportacionParcialActualizaImporteSinCompletar() {
        Usuario u = usuarioService.register("obj-parcial@x.com", "Obj", "password123");
        Objetivo o = service.create(u, "Vacaciones", new BigDecimal("1000.00"), LocalDate.now().plusMonths(6), null);

        service.aportar(u, o.getId(), new BigDecimal("300.00"));
        Objetivo refreshed = service.get(u, o.getId());

        assertEquals(0, refreshed.getImporteAhorrado().compareTo(new BigDecimal("300.00")));
        assertFalse(refreshed.isCompletado());
        assertEquals(0, refreshed.getPorcentaje().compareTo(new BigDecimal("30.0")));
    }

    @Test
    void aportacionQueAlcanzaObjetivoLoMarcaCompletado() {
        Usuario u = usuarioService.register("obj-completo@x.com", "Obj", "password123");
        Objetivo o = service.create(u, "Móvil", new BigDecimal("500.00"), null, null);

        service.aportar(u, o.getId(), new BigDecimal("500.00"));
        Objetivo refreshed = service.get(u, o.getId());

        assertTrue(refreshed.isCompletado());
        assertNotNull(refreshed.getCompletadoAt());
    }

    @Test
    void noPermiteAccederAObjetivoDeOtroUsuario() {
        Usuario u1 = usuarioService.register("u1obj@x.com", "U1", "password123");
        Usuario u2 = usuarioService.register("u2obj@x.com", "U2", "password123");
        Objetivo o = service.create(u1, "X", new BigDecimal("100"), null, null);
        assertThrows(ObjetivoService.ObjetivoNotFoundException.class,
            () -> service.get(u2, o.getId()));
    }
}
