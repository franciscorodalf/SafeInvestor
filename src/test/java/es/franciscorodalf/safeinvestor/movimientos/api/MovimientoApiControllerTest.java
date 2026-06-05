package es.franciscorodalf.safeinvestor.movimientos.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.franciscorodalf.safeinvestor.auth.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MovimientoApiControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioService usuarioService;

    @BeforeEach
    void crearUsuario() {
        try { usuarioService.register("apiuser@x.com", "Api", "password123"); }
        catch (Exception ignored) {}
    }

    @Test
    @WithMockUser(username = "apiuser@x.com")
    void creaYListaMovimiento() throws Exception {
        var body = Map.of(
            "tipo", "GASTO",
            "importe", "25.50",
            "descripcion", "Test",
            "fecha", "2026-06-05"
        );
        mvc.perform(post("/api/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.importe").value(25.50));

        mvc.perform(get("/api/movimientos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].tipo").value("GASTO"));
    }

    @Test
    void sinAuthDevuelve401() throws Exception {
        mvc.perform(get("/api/movimientos"))
            .andExpect(status().isUnauthorized());
    }
}
