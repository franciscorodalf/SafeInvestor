package es.franciscorodalf.saveinvestor.backend.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import es.franciscorodalf.saveinvestor.backend.model.Usuario;

class UsuarioDAOTest {
    @InjectMocks
    private UsuarioDAO usuarioDAO;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        usuarioDAO = spy(new UsuarioDAO());
        doReturn(connection).when(usuarioDAO).conectar();
    }

    @Test
    void testInsertar() throws Exception {
        Usuario usuario = new Usuario("Carlos", "carlos@email.com", "1234");

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        when(stmt.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(99);

        usuarioDAO.insertar(usuario);

        // Corregir el orden de los parámetros para que coincida con la implementación real
        verify(stmt).setString(1, "carlos@email.com");
        verify(stmt).setString(2, "Carlos");
        verify(stmt).setString(3, "1234");
        verify(stmt).executeUpdate();
        assertEquals(99, usuario.getId());
    }

    @Test
    void testActualizar() throws Exception {
        Usuario usuario = new Usuario("Ana", "ana@mail.com", "clave");
        usuario.setId(10);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        usuarioDAO.actualizar(usuario);

        // Corregir el orden de los parámetros para que coincida con la implementación real
        verify(stmt).setString(1, "ana@mail.com");
        verify(stmt).setString(2, "Ana");
        verify(stmt).setString(3, "clave");
        verify(stmt).setInt(4, 10);
        verify(stmt).executeUpdate();
    }

    @Test
    void testEliminar() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);

        usuarioDAO.eliminar(5);

        verify(stmt).setInt(1, 5);
        verify(stmt).executeUpdate();
    }

    @Test
    void testObtenerPorId() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("nombre")).thenReturn("María");
        when(rs.getString("email")).thenReturn("maria@mail.com");
        when(rs.getString("contrasenia")).thenReturn("pass");

        Usuario result = usuarioDAO.obtenerPorId(1);

        assertNotNull(result);
        assertEquals("María", result.getNombre());
    }

    @Test
    void testObtenerTodos() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("nombre")).thenReturn("Luis");
        when(rs.getString("email")).thenReturn("luis@mail.com");
        when(rs.getString("contrasenia")).thenReturn("clave");

        List<Usuario> usuarios = usuarioDAO.obtenerTodos();

        assertEquals(1, usuarios.size());
        assertEquals("Luis", usuarios.get(0).getNombre());
    }

    @Test
    void testAutenticar() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(2);
        when(rs.getString("nombre")).thenReturn("jorge");
        when(rs.getString("email")).thenReturn("jorge@email.com");
        when(rs.getString("contrasenia")).thenReturn("123");

        Usuario usuario = usuarioDAO.autenticar("jorge", "123");

        assertNotNull(usuario);
        assertEquals("jorge", usuario.getNombre());
        assertEquals("123", usuario.getContrasenia());
    }
}