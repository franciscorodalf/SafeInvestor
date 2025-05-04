package es.franciscorodalf.saveinvestor.backend.dao;

import es.franciscorodalf.saveinvestor.backend.model.tarea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import java.util.List;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TareaDAOTest {

    @InjectMocks
    private TareaDAO tareaDAO;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement stmt;

    @Mock
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        tareaDAO = spy(new TareaDAO());
        doReturn(connection).when(tareaDAO).conectar();
    }

    @Test
    void testInsertar() throws Exception {
        tarea t = new tarea("concepto test", 100.0, new Date(), "INGRESO", 1);

        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        when(stmt.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(42);

        tareaDAO.insertar(t);

        verify(stmt).setString(1, "concepto test");
        verify(stmt).setDouble(2, 100.0);
        verify(stmt).executeUpdate();
        assertEquals(42, t.getId());
    }

    @Test
    void testActualizar() throws Exception {
        tarea t = new tarea("nuevo concepto", 200.0, new Date(), "GASTO", 1);
        t.setId(10);

        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);

        tareaDAO.actualizar(t);

        verify(stmt).setString(1, "nuevo concepto");
        verify(stmt).setDouble(2, 200.0);
        verify(stmt).setInt(5, 10);
        verify(stmt).executeUpdate();
    }

    @Test
    void testEliminar() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);

        tareaDAO.eliminar(5);

        verify(stmt).setInt(1, 5);
        verify(stmt).executeUpdate();
    }

    @Test
    void testObtenerPorId() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("concepto")).thenReturn("Test");
        when(rs.getDouble("cantidad")).thenReturn(123.0);
        when(rs.getTimestamp("fecha")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(rs.getString("estado")).thenReturn("INGRESO");
        when(rs.getInt("usuario_id")).thenReturn(2);

        tarea result = tareaDAO.obtenerPorId(1);

        assertNotNull(result);
        assertEquals("Test", result.getConcepto());
    }

    @Test
    void testObtenerPorUsuario() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("concepto")).thenReturn("Compra");
        when(rs.getDouble("cantidad")).thenReturn(20.0);
        when(rs.getTimestamp("fecha")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(rs.getString("estado")).thenReturn("GASTO");
        when(rs.getInt("usuario_id")).thenReturn(1);

        List<tarea> lista = tareaDAO.obtenerPorUsuario(1);

        assertEquals(1, lista.size());
    }

    @Test
    void testCalcularTotalPorTipoYPeriodo() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getDouble("total")).thenReturn(300.0);

        double total = tareaDAO.calcularTotalPorTipoYPeriodo(1, "INGRESO", new Date(), new Date());

        assertEquals(300.0, total);
    }

    @Test
    void testObtenerUltimasTareas() throws Exception {
        when(connection.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id")).thenReturn(1);
        when(rs.getString("concepto")).thenReturn("Test");
        when(rs.getDouble("cantidad")).thenReturn(50.0);
        when(rs.getTimestamp("fecha")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(rs.getString("estado")).thenReturn("INGRESO");
        when(rs.getInt("usuario_id")).thenReturn(1);

        List<tarea> tareas = tareaDAO.obtenerUltimasTareas(1, 5);

        assertEquals(1, tareas.size());
    }
}
