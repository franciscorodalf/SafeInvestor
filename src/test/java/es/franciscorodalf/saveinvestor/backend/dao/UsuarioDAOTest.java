package es.franciscorodalf.saveinvestor.backend.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.franciscorodalf.saveinvestor.backend.model.Usuario;

class UsuarioDAOTest {

    private UsuarioDAO usuarioDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private Statement mockStatement;

    @BeforeEach
    void setUp() throws SQLException {
        // Crear mocks para los objetos de base de datos
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockStatement = mock(Statement.class);
        
        // Configurar comportamiento general
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        
        // Crear la instancia a testear con una subclase que sobreescribe getConnection
        usuarioDAO = new UsuarioDAO() {
            @Override
            public Connection getConnection() {
                return mockConnection;
            }
        };
    }

    @Test
    void testInsertar() throws SQLException {
        // Preparar datos de prueba
        Usuario usuario = new Usuario();
        usuario.setNombre("testUser");
        usuario.setEmail("test@example.com");
        usuario.setContrasenia("password123");
        
        // Configurar mock para simular generación de ID
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);
        
        // Ejecutar método a probar
        usuarioDAO.insertar(usuario);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setString(1, "testUser");
        verify(mockPreparedStatement).setString(2, "test@example.com");
        verify(mockPreparedStatement).setString(3, "password123");
        verify(mockPreparedStatement).executeUpdate();
        
        // Verificar que el ID se actualizó
        assertEquals(1, usuario.getId());
    }

    @Test
    void testAutenticar_Exitoso() throws SQLException {
        // Configurar comportamiento del ResultSet para simular un usuario encontrado
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("nombre")).thenReturn("testUser");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getString("contrasenia")).thenReturn("password123");
        
        // Ejecutar método a probar
        Usuario resultado = usuarioDAO.autenticar("test@example.com", "password123");
        
        // Verificar comportamiento y resultado
        verify(mockPreparedStatement).setString(1, "test@example.com");
        verify(mockPreparedStatement).setString(2, "password123");
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("testUser", resultado.getNombre());
        assertEquals("test@example.com", resultado.getEmail());
    }
    
    @Test
    void testAutenticar_Fallido() throws SQLException {
        // Configurar comportamiento del ResultSet para simular que no se encontró usuario
        when(mockResultSet.next()).thenReturn(false);
        
        // Ejecutar método a probar
        Usuario resultado = usuarioDAO.autenticar("invalid@example.com", "wrongPassword");
        
        // Verificar comportamiento y resultado
        verify(mockPreparedStatement).setString(1, "invalid@example.com");
        verify(mockPreparedStatement).setString(2, "wrongPassword");
        
        assertNull(resultado);
    }
    
    @Test
    void testActualizar() throws SQLException {
        // Preparar datos de prueba
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setNombre("updatedUser");
        usuario.setEmail("updated@example.com");
        usuario.setContrasenia("newPassword");
        
        // Configurar mock
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        // Ejecutar método a probar
        usuarioDAO.actualizar(usuario);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setString(1, "updatedUser");
        verify(mockPreparedStatement).setString(2, "updated@example.com");
        verify(mockPreparedStatement).setString(3, "newPassword");
        verify(mockPreparedStatement).setInt(4, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void testEliminar() throws SQLException {
        // Configurar mock
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        // Ejecutar método a probar
        usuarioDAO.eliminar(1);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setInt(1, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
    
    @Test
    void testObtenerPorId_Encontrado() throws SQLException {
        // Configurar comportamiento del ResultSet para simular un usuario encontrado
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("nombre")).thenReturn("testUser");
        when(mockResultSet.getString("email")).thenReturn("test@example.com");
        when(mockResultSet.getString("contrasenia")).thenReturn("password123");
        
        // Ejecutar método a probar
        Usuario resultado = usuarioDAO.obtenerPorId(1);
        
        // Verificar comportamiento y resultado
        verify(mockPreparedStatement).setInt(1, 1);
        
        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals("testUser", resultado.getNombre());
        assertEquals("test@example.com", resultado.getEmail());
    }
    
    @Test
    void testObtenerPorId_NoEncontrado() throws SQLException {
        // Configurar comportamiento del ResultSet para simular que no se encontró usuario
        when(mockResultSet.next()).thenReturn(false);
        
        // Ejecutar método a probar
        Usuario resultado = usuarioDAO.obtenerPorId(999);
        
        // Verificar comportamiento y resultado
        verify(mockPreparedStatement).setInt(1, 999);
        
        assertNull(resultado);
    }
}
