package es.franciscorodalf.safeinvestor.objetivos.api;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ObjetivoApiControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioService usuarioService;

    @BeforeEach
    void crearUsuario() {
        try { usuarioService.register("objapi@x.com", "ObjApi", "password123"); }
        catch (Exception ignored) {}
    }

    @Test
    @WithMockUser(username = "objapi@x.com")
    void creaListaYAportaAObjetivo() throws Exception {
        var body = Map.of("nombre", "Coche", "importeObjetivo", "5000.00");
        MvcResult res = mvc.perform(post("/api/objetivos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Coche"))
            .andExpect(jsonPath("$.completado").value(false))
            .andReturn();

        Long id = (Integer.toUnsignedLong((Integer) json.readValue(
            res.getResponse().getContentAsString(), Map.class).get("id")));

        mvc.perform(post("/api/objetivos/" + id + "/aportar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("importe", "5000.00"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completado").value(true));

        mvc.perform(get("/api/objetivos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void sinAuthDevuelve401() throws Exception {
        mvc.perform(get("/api/objetivos"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/tareas"))
            .andExpect(status().isUnauthorized());
    }
}
