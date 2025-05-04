package es.franciscorodalf.saveinvestor.backend.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import es.franciscorodalf.saveinvestor.backend.model.Objetivo;

class ObjetivoDaoTest {
    @InjectMocks
    private ObjetivoDAO objetivoDAO;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        objetivoDAO = spy(new ObjetivoDAO());
        doReturn(connection).when(objetivoDAO).conectar();
    }

  

    @Test
    void testEliminar() throws Exception {
        // Configurar los mocks
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        // Ejecutar el método a probar
        objetivoDAO.eliminar(5);

        // Verificar que el ID se estableció correctamente
        verify(stmt).setInt(1, 5);
        verify(stmt).executeUpdate();
    }

    @Test
    void testObtenerPorId() throws Exception {
        // Configurar fecha para prueba
        Date fechaInicio = new Date();
        Date fechaFin = new Date(fechaInicio.getTime() + 3600000);

        // Configurar los mocks
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("descripcion")).thenReturn("Viaje a Europa");
        when(rs.getDouble("cantidad_objetivo")).thenReturn(5000.0);
        when(rs.getDouble("cantidad_actual")).thenReturn(2000.0);
        when(rs.getDate("fecha_inicio")).thenReturn(new java.sql.Date(fechaInicio.getTime()));
        when(rs.getDate("fecha_objetivo")).thenReturn(new java.sql.Date(fechaFin.getTime()));
        when(rs.getInt("usuario_id")).thenReturn(2);
        when(rs.getTimestamp("fecha_creacion")).thenReturn(new java.sql.Timestamp(fechaInicio.getTime()));
        when(rs.getInt("completado")).thenReturn(0);

        // Ejecutar el método a probar
        Objetivo result = objetivoDAO.obtenerPorId(1);

        // Verificar resultado
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Viaje a Europa", result.getDescripcion());
        // Corregir los nombres de los getters para que coincidan con la implementación
        // real
        assertEquals(5000.0, result.getCantidadObjetivo());
        assertEquals(2000.0, result.getCantidadActual());
        assertEquals(2, result.getUsuarioId());
    }

    @Test
    void testObtenerTodos() throws Exception {
        // Configurar fecha para prueba
        Date fechaInicio = new Date();
        Date fechaFin = new Date(fechaInicio.getTime() + 3600000);

        // Configurar los mocks
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false); // Solo devolver un registro
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("descripcion")).thenReturn("Fondo de emergencia");
        when(rs.getDouble("cantidad_objetivo")).thenReturn(10000.0);
        when(rs.getDouble("cantidad_actual")).thenReturn(5000.0);
        when(rs.getDate("fecha_objetivo")).thenReturn(new java.sql.Date(fechaFin.getTime()));
        when(rs.getTimestamp("fecha_creacion")).thenReturn(new java.sql.Timestamp(fechaInicio.getTime()));
        when(rs.getInt("usuario_id")).thenReturn(1);
        when(rs.getInt("completado")).thenReturn(0);

        // Ejecutar el método a probar
        List<Objetivo> objetivos = objetivoDAO.obtenerTodos();

        // Verificar resultado
        assertEquals(1, objetivos.size());
        assertEquals("Fondo de emergencia", objetivos.get(0).getDescripcion());
        // Corregir el nombre del getter
        assertEquals(10000.0, objetivos.get(0).getCantidadObjetivo());
    }

    @Test
    void testObtenerPorUsuario() throws Exception {
        // Configurar fecha para prueba
        Date fechaInicio = new Date();
        Date fechaFin = new Date(fechaInicio.getTime() + 3600000);

        // Configurar los mocks
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false); // Devolver dos registros
        when(rs.getInt("id")).thenReturn(1, 2);
        when(rs.getString("descripcion")).thenReturn("Objetivo 1", "Objetivo 2");
        when(rs.getDouble("cantidad_objetivo")).thenReturn(10000.0, 20000.0);
        when(rs.getDouble("cantidad_actual")).thenReturn(5000.0, 8000.0);
        when(rs.getDate("fecha_objetivo")).thenReturn(new java.sql.Date(fechaFin.getTime()));
        when(rs.getTimestamp("fecha_creacion")).thenReturn(new java.sql.Timestamp(fechaInicio.getTime()));
        when(rs.getInt("usuario_id")).thenReturn(3, 3);
        when(rs.getInt("completado")).thenReturn(0, 0);

        // Ejecutar el método a probar
        List<Objetivo> objetivos = objetivoDAO.obtenerPorUsuario(3);

        // Verificar resultado
        assertEquals(2, objetivos.size());
        assertEquals("Objetivo 1", objetivos.get(0).getDescripcion());
        assertEquals("Objetivo 2", objetivos.get(1).getDescripcion());
        // Los nombres de getters ya están correctos
        assertEquals(3, objetivos.get(0).getUsuarioId());
        assertEquals(3, objetivos.get(1).getUsuarioId());
    }
}
