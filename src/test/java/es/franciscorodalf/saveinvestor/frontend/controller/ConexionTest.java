package es.franciscorodalf.saveinvestor.frontend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;

class ConexionTest {

    private Connection mockConnection;

    static class ConexionDummy extends Conexion {
        private Connection mockedConnection;

        public ConexionDummy(Connection mockedConnection) {
            this.mockedConnection = mockedConnection;
        }

        @Override
        public Connection getConnection() {
            return mockedConnection;
        }
    }

    private ConexionDummy conexion;

    @BeforeEach
    void setUp() {
        mockConnection = mock(Connection.class);
        conexion = new ConexionDummy(mockConnection);
    }

    @Test
    void testConectar() throws SQLException {
        Connection conn = conexion.getConnection();
        assertNotNull(conn);
        assertEquals(mockConnection, conn);
    }

    @Test
    void testObtenerRutaArchivoBD() {
        String rutaPorDefecto = "src/main/resources/database/saveinvestor.db";
        assertEquals(rutaPorDefecto, conexion.getRutaArchivoBD());
    }

    @Test
    void testCerrarConexion() throws SQLException {
        // Usamos reflección para establecer el campo estático connection
        Connection mockConn = mock(Connection.class);
        when(mockConn.isClosed()).thenReturn(false);
        
        // Establecemos la conexión usando el método estático
        java.lang.reflect.Field field;
        try {
            field = Conexion.class.getDeclaredField("connection");
            field.setAccessible(true);
            field.set(null, mockConn);
            
            // Ahora cerramos
            Conexion.cerrar();
            
            // Y verificamos que cerró correctamente
            verify(mockConn, times(1)).close();
            
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Error al acceder al campo connection: " + e.getMessage());
        }
    }

    @Test
    void testCerrarConexionYaCerrada() throws SQLException {
        // Si la conexión ya está cerrada, no debería intentar cerrarla de nuevo
        Connection mockConn = mock(Connection.class);
        when(mockConn.isClosed()).thenReturn(true);
        
        try {
            java.lang.reflect.Field field = Conexion.class.getDeclaredField("connection");
            field.setAccessible(true);
            field.set(null, mockConn);
            
            Conexion.cerrar();
            
            verify(mockConn, never()).close();
            
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Error al acceder al campo connection: " + e.getMessage());
        }
    }
}