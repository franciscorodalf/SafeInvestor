package es.franciscorodalf.saveinvestor.backend.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.franciscorodalf.saveinvestor.backend.model.tarea;

class TareaDAOTest {

    private TareaDAO tareaDAO;
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
        tareaDAO = new TareaDAO() {
            @Override
            public Connection getConnection() {
                return mockConnection;
            }
        };
    }

    @Test
    void testInsertar() throws SQLException {
        // Preparar datos de prueba
        java.util.Date fechaActual = new java.util.Date();
        tarea nuevaTarea = new tarea("Pago salario", 1500.0, fechaActual, tarea.ESTADO_INGRESO, 1);
        
        // Configurar mock para simular generación de ID
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);
        
        // Ejecutar método a probar
        tareaDAO.insertar(nuevaTarea);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setString(1, "Pago salario");
        verify(mockPreparedStatement).setDouble(2, 1500.0);
        verify(mockPreparedStatement).setDate(eq(3), any(Date.class));
        verify(mockPreparedStatement).setString(4, tarea.ESTADO_INGRESO);
        verify(mockPreparedStatement).setInt(5, 1);
        verify(mockPreparedStatement).executeUpdate();
        
        // Verificar que el ID se actualizó
        assertEquals(1, nuevaTarea.getId());
    }

    @Test
    void testObtenerPorUsuario() throws SQLException {
        // Configurar comportamiento del ResultSet para simular múltiples tareas
        when(mockResultSet.next())
            .thenReturn(true)  // Primera llamada: hay resultados
            .thenReturn(true)  // Segunda llamada: hay más resultados
            .thenReturn(false); // Tercera llamada: no hay más resultados
        
        // Configurar datos para la primera tarea
        when(mockResultSet.getInt("id"))
            .thenReturn(1)  // Primera llamada
            .thenReturn(2); // Segunda llamada
            
        when(mockResultSet.getString("concepto"))
            .thenReturn("Ingreso")  // Primera llamada
            .thenReturn("Gasto");   // Segunda llamada
            
        when(mockResultSet.getDouble("cantidad"))
            .thenReturn(1000.0)    // Primera llamada
            .thenReturn(500.0);    // Segunda llamada
            
        when(mockResultSet.getDate("fecha"))
            .thenReturn(new Date(System.currentTimeMillis()))  // Primera llamada
            .thenReturn(new Date(System.currentTimeMillis())); // Segunda llamada
            
        when(mockResultSet.getString("estado"))
            .thenReturn(tarea.ESTADO_INGRESO)  // Primera llamada
            .thenReturn(tarea.ESTADO_GASTO);   // Segunda llamada
            
        when(mockResultSet.getInt("usuario_id"))
            .thenReturn(1)  // Primera llamada
            .thenReturn(1); // Segunda llamada
        
        // Ejecutar método a probar
        List<tarea> tareas = tareaDAO.obtenerPorUsuario(1);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setInt(1, 1);
        
        // Verificar resultados
        assertEquals(2, tareas.size());
        assertEquals("Ingreso", tareas.get(0).getConcepto());
        assertEquals("Gasto", tareas.get(1).getConcepto());
        assertEquals(1000.0, tareas.get(0).getCantidad());
        assertEquals(500.0, tareas.get(1).getCantidad());
        assertEquals(tarea.ESTADO_INGRESO, tareas.get(0).getEstado());
        assertEquals(tarea.ESTADO_GASTO, tareas.get(1).getEstado());
    }
    
    @Test
    void testActualizar() throws SQLException {
        // Preparar datos de prueba
        tarea tareaExistente = new tarea();
        tareaExistente.setId(1);
        tareaExistente.setConcepto("Actualizado");
        tareaExistente.setCantidad(2000.0);
        tareaExistente.setFecha(new java.util.Date());
        tareaExistente.setEstado(tarea.ESTADO_GASTO);
        
        // Configurar mock
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        // Ejecutar método a probar
        tareaDAO.actualizar(tareaExistente);
        
        // Verificar comportamiento
        verify(mockPreparedStatement).setString(1, "Actualizado");
        verify(mockPreparedStatement).setDouble(2, 2000.0);
        verify(mockPreparedStatement).setDate(eq(3), any(Date.class));
        verify(mockPreparedStatement).setString(4, tarea.ESTADO_GASTO);
        verify(mockPreparedStatement).setInt(5, 1);
        verify(mockPreparedStatement).executeUpdate();
    }
}
