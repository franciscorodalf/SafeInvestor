package es.franciscorodalf.saveinvestor.backend.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.franciscorodalf.saveinvestor.backend.model.estadistica;

class EstadisticaDAOTest {

    private EstadisticaDAO estadisticaDAO;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    
    @BeforeEach
    void setUp() throws SQLException {
        // Crear mocks para los objetos de base de datos
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        
        // Configurar comportamiento general
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        
        // Crear la instancia a testear con una subclase que sobreescribe getConnection
        estadisticaDAO = new EstadisticaDAO() {
            @Override
            public Connection getConnection() {
                return mockConnection;
            }
        };
    }

    @Test
    void testInsertar() throws SQLException {
        // Preparar datos de prueba
        estadistica nuevaEstadistica = new estadistica(1000.0, 500.0, 1);
        
        // Configurar mock para simular generación de ID
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);
        
        // Ejecutar método a probar
        estadisticaDAO.insertar(nuevaEstadistica);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setDouble(1, 1000.0);
        verify(mockPreparedStatement).setDouble(2, 500.0);
        verify(mockPreparedStatement).setInt(3, 1);
        verify(mockPreparedStatement).executeUpdate();
        
        // Verificar que el ID se actualizó
        assertEquals(1, nuevaEstadistica.getId());
    }

    @Test
    void testObtenerPorUsuario_Encontrado() throws SQLException {
        // Configurar comportamiento del ResultSet para simular una estadística encontrada
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getDouble("total_ingreso")).thenReturn(1000.0);
        when(mockResultSet.getDouble("total_gasto")).thenReturn(500.0);
        when(mockResultSet.getInt("usuario_id")).thenReturn(1);
        
        // Ejecutar método a probar
        estadistica resultado = estadisticaDAO.obtenerPorUsuario(1);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setInt(1, 1);
        
        // Verificar resultado
        assertNotNull(resultado);
        assertEquals(1, resultado.getId());
        assertEquals(1000.0, resultado.getTotalIngreso());
        assertEquals(500.0, resultado.getTotalGasto());
        assertEquals(1, resultado.getUsuarioId());
    }
    
    @Test
    void testObtenerPorUsuario_NoEncontrado() throws SQLException {
        // Configurar comportamiento del ResultSet para simular que no se encontró estadística
        when(mockResultSet.next()).thenReturn(false);
        
        // Ejecutar método a probar
        estadistica resultado = estadisticaDAO.obtenerPorUsuario(999);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setInt(1, 999);
        
        // Verificar resultado
        assertNull(resultado);
    }
    
    @Test
    void testActualizar() throws SQLException {
        // Preparar datos de prueba
        estadistica estadisticaActualizada = new estadistica();
        estadisticaActualizada.setId(1);
        estadisticaActualizada.setTotalIngreso(2000.0);
        estadisticaActualizada.setTotalGasto(1000.0);
        
        // Configurar mock
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        // Ejecutar método a probar
        estadisticaDAO.actualizar(estadisticaActualizada);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setDouble(1, 2000.0);
        verify(mockPreparedStatement).setDouble(2, 1000.0);
        verify(mockPreparedStatement).setInt(3, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
}
