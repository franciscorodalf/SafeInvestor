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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import es.franciscorodalf.saveinvestor.backend.model.estadistica;

class EstadisticaDAOTest {

    @InjectMocks
    private EstadisticaDAO estadisticaDAO;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        // Hacemos que Conexion::conectar devuelva nuestra conexión mockeada
        estadisticaDAO = spy(new EstadisticaDAO());
        doReturn(connection).when(estadisticaDAO).conectar();
    }

    @Test
    void testInsertar() throws Exception {
        estadistica stats = new estadistica(100.0, 50.0, 1);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        when(stmt.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(123);

        estadisticaDAO.insertar(stats);

        verify(stmt).setDouble(1, 100.0);
        verify(stmt).setDouble(2, 50.0);
        verify(stmt).setInt(3, 1);
        verify(stmt).executeUpdate();
        assertEquals(123, stats.getId());
    }

    @Test
    void testActualizar() throws Exception {
        estadistica stats = new estadistica(200.0, 75.0, 1);
        stats.setId(5); // Agregar ID para la actualización

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        estadisticaDAO.actualizar(stats);

        verify(stmt).setDouble(1, 200.0);
        verify(stmt).setDouble(2, 75.0);
        verify(stmt).setInt(3, 1);
        verify(stmt).executeUpdate();
    }

    @Test
    void testEliminar() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);

        estadisticaDAO.eliminar(10);

        verify(stmt).setInt(1, 10);
        verify(stmt).executeUpdate();
    }

    @Test
    void testObtenerPorId() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getDouble("total_ingreso")).thenReturn(500.0);
        when(rs.getDouble("total_gasto")).thenReturn(250.0);
        when(rs.getInt("usuario_id")).thenReturn(2);

        estadistica result = estadisticaDAO.obtenerPorId(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(500.0, result.getTotalIngreso());
        assertEquals(250.0, result.getTotalGasto());
        assertEquals(2, result.getUsuarioId());
    }

    @Test
    void testObtenerTodos() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getDouble("total_ingreso")).thenReturn(100.0);
        when(rs.getDouble("total_gasto")).thenReturn(50.0);
        when(rs.getInt("usuario_id")).thenReturn(1);

        List<estadistica> resultados = estadisticaDAO.obtenerTodos();

        assertEquals(1, resultados.size());
        assertEquals(1, resultados.get(0).getId());
    }

    @Test
    void testIncrementarIngresos_Existente() throws Exception {
        estadistica stats = new estadistica(100.0, 0.0, 1);
        stats.setId(1);

        doReturn(stats).when(estadisticaDAO).obtenerPorUsuario(1);
        doNothing().when(estadisticaDAO).actualizar(any());

        estadisticaDAO.incrementarIngresos(1, 50.0);

        assertEquals(150.0, stats.getTotalIngreso());
    }

    @Test
    void testIncrementarIngresos_Nuevo() throws Exception {
        doReturn(null).when(estadisticaDAO).obtenerPorUsuario(2);
        doNothing().when(estadisticaDAO).insertar(any());

        estadisticaDAO.incrementarIngresos(2, 80.0);

        verify(estadisticaDAO).insertar(argThat(e ->
            e.getTotalIngreso() == 80.0 &&
            e.getTotalGasto() == 0.0 &&
            e.getUsuarioId() == 2
        ));
    }

    @Test
    void testIncrementarGastos_Existente() throws Exception {
        estadistica stats = new estadistica(0.0, 30.0, 1);
        stats.setId(1);

        doReturn(stats).when(estadisticaDAO).obtenerPorUsuario(1);
        doNothing().when(estadisticaDAO).actualizar(any());

        estadisticaDAO.incrementarGastos(1, 20.0);

        assertEquals(50.0, stats.getTotalGasto());
        verify(estadisticaDAO).actualizar(stats);
    }

    @Test
    void testIncrementarGastos_Nuevo() throws Exception {
        doReturn(null).when(estadisticaDAO).obtenerPorUsuario(2);
        doNothing().when(estadisticaDAO).insertar(any());

        estadisticaDAO.incrementarGastos(2, 40.0);

        verify(estadisticaDAO).insertar(argThat(e ->
            e.getTotalIngreso() == 0.0 &&
            e.getTotalGasto() == 40.0 &&
            e.getUsuarioId() == 2
        ));
    }
}