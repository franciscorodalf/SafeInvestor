package es.franciscorodalf.saveinvestor.backend.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void testConstructorVacio() {
        Usuario usuario = new Usuario();
        assertNull(usuario.getId());
        assertNull(usuario.getNombre());
        assertNull(usuario.getEmail());
        assertNull(usuario.getContrasenia());
    }
    
    @Test
    void testConstructorParametrizado() {
        Usuario usuario = new Usuario("test@example.com", "TestUser", "password123");
        assertNull(usuario.getId());
        assertEquals("TestUser", usuario.getNombre());
        assertEquals("test@example.com", usuario.getEmail());
        assertEquals("password123", usuario.getContrasenia());
    }
    
    @Test
    void testSettersAndGetters() {
        Usuario usuario = new Usuario();
        
        usuario.setId(1);
        usuario.setNombre("John Doe");
        usuario.setEmail("john@example.com");
        usuario.setContrasenia("securePassword");
        
        assertEquals(1, usuario.getId());
        assertEquals("John Doe", usuario.getNombre());
        assertEquals("john@example.com", usuario.getEmail());
        assertEquals("securePassword", usuario.getContrasenia());
    }
    
    @Test
    void testMethodChaining() {
        Usuario usuario = new Usuario()
            .email("jane@example.com")
            .nombre("Jane Smith")
            .contrasenia("jane123");
            
        assertEquals("Jane Smith", usuario.getNombre());
        assertEquals("jane@example.com", usuario.getEmail());
        assertEquals("jane123", usuario.getContrasenia());
    }
    
    @Test
    void testEquals_IdenticalUsers() {
        Usuario usuario1 = new Usuario("test@example.com", "TestUser", "password123");
        usuario1.setId(1);
        
        Usuario usuario2 = new Usuario("test@example.com", "TestUser", "password123");
        usuario2.setId(1);
        
        assertTrue(usuario1.equals(usuario2));
        assertEquals(usuario1.hashCode(), usuario2.hashCode());
    }
    
    @Test
    void testEquals_DifferentUsers() {
        Usuario usuario1 = new Usuario("test1@example.com", "User1", "password1");
        usuario1.setId(1);
        
        Usuario usuario2 = new Usuario("test2@example.com", "User2", "password2");
        usuario2.setId(2);
        
        assertFalse(usuario1.equals(usuario2));
        assertNotEquals(usuario1.hashCode(), usuario2.hashCode());
    }
    
    @Test
    void testEquals_DifferentTypes() {
        Usuario usuario = new Usuario("test@example.com", "TestUser", "password123");
        
        assertFalse(usuario.equals(new Object()));
    }
    
    @Test
    void testToString() {
        Usuario usuario = new Usuario("test@example.com", "TestUser", "password123");
        usuario.setId(1);
        
        String expectedString = "{" +
            " id='1'" +
            ", email='test@example.com'" +
            ", nombre='TestUser'" +
            ", contrasenia='password123'" +
            "}";
            
        assertEquals(expectedString, usuario.toString());
    }
}
