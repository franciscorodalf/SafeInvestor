package es.franciscorodalf.safeinvestor.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.franciscorodalf.safeinvestor.auth.api.dto.LoginRequest;
import es.franciscorodalf.safeinvestor.auth.api.dto.RegisterRequest;
import es.franciscorodalf.safeinvestor.auth.domain.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UsuarioRepository repo;

    @BeforeEach
    void clean() { repo.deleteAll(); }

    @Test
    void registerThenLoginReturnsToken() throws Exception {
        var register = new RegisterRequest("test@x.com", "Test", "password123");
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(register)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.email").value("test@x.com"));

        var login = new LoginRequest("test@x.com", "password123");
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        var req = new RegisterRequest("dup@x.com", "Dup", "password123");
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(req)))
            .andExpect(status().isConflict());
    }

    @Test
    void badCredentialsReturn401() throws Exception {
        var bad = new LoginRequest("nobody@x.com", "wrong");
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(bad)))
            .andExpect(status().isUnauthorized());
    }
}
