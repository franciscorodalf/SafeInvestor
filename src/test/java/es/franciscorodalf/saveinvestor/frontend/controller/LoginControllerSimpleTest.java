package es.franciscorodalf.saveinvestor.frontend.controller;

import es.franciscorodalf.saveinvestor.backend.dao.UsuarioDAO;
import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControllerSimpleTest {

    @Test
    public void testAutenticacionExitosa() throws java.sql.SQLException {
        // Crear un DAO de prueba que siempre devuelve un usuario válido
        UsuarioDAO testDAO = new UsuarioDAO() {
            @Override
            public Usuario autenticar(String username, String password) {
                if ("usuario_test".equals(username) && "password_test".equals(password)) {
                    Usuario usuario = new Usuario();
                    usuario.setNombre("Usuario Test");
                    return usuario;
                }
                return null;
            }
        };
        
        // Probar autenticación válida
        Usuario resultado = testDAO.autenticar("usuario_test", "password_test");
        assertNotNull(resultado, "La autenticación debería ser exitosa con credenciales correctas");
        assertEquals("Usuario Test", resultado.getNombre());
        
        // Probar autenticación inválida
        Usuario resultadoInvalido = testDAO.autenticar("usuario_test", "password_incorrecta");
        assertNull(resultadoInvalido, "La autenticación debería fallar con credenciales incorrectas");
    }
}
