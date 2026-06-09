package es.franciscorodalf.safeinvestor.tareas.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import es.franciscorodalf.safeinvestor.tareas.domain.Tarea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TareaServiceTest {

    @Autowired UsuarioService usuarioService;
    @Autowired TareaService service;

    @Test
    void completarYDescompletar() {
        Usuario u = usuarioService.register("tareas-flow@x.com", "T", "password123");
        Tarea t = service.create(u, "Pagar alquiler", "antes del día 5", LocalDate.now().plusDays(7));

        assertFalse(t.isCompletada());
        service.completar(u, t.getId());
        assertTrue(service.get(u, t.getId()).isCompletada());

        service.descompletar(u, t.getId());
        assertFalse(service.get(u, t.getId()).isCompletada());
    }

    @Test
    void tareaVencidaSinCompletarFlagsVencida() {
        Usuario u = usuarioService.register("tareas-venc@x.com", "T", "password123");
        Tarea t = service.create(u, "Vieja", null, LocalDate.now().minusDays(1));
        assertTrue(service.get(u, t.getId()).isVencida());
    }
}
