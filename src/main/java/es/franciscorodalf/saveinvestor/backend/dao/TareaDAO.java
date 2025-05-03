package es.franciscorodalf.saveinvestor.backend.dao;

import es.franciscorodalf.saveinvestor.backend.model.estadistica;
import es.franciscorodalf.saveinvestor.backend.model.tarea;
import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TareaDAO extends Conexion implements DAO<tarea> {

    @Override
    public void insertar(tarea tarea) throws SQLException {
        String sql = "INSERT INTO tarea (concepto, cantidad, fecha, estado, usuario_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, tarea.getConcepto());
            stmt.setDouble(2, tarea.getCantidad());
            stmt.setDate(3, new java.sql.Date(tarea.getFecha().getTime()));
            stmt.setString(4, tarea.getEstado());
            stmt.setInt(5, tarea.getUsuarioId());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                tarea.setId(rs.getInt(1));
            }
        }
    }

    /**
     * Inserta una nueva tarea sin activar los triggers de la base de datos
     */
    public void insertarSinTrigger(tarea tarea) throws SQLException {
        Connection conn = null;
        try {
            conn = conectar();

            // Desactivar temporalmente las restricciones
            try (Statement stmtPragma1 = conn.createStatement()) {
                stmtPragma1.execute("PRAGMA ignore_check_constraints = ON;");
            }

            // Ejecutar la inserción
            String sqlInsert = "INSERT INTO tarea (concepto, cantidad, fecha, estado, usuario_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                stmtInsert.setString(1, tarea.getConcepto());
                stmtInsert.setDouble(2, tarea.getCantidad());
                stmtInsert.setDate(3, new java.sql.Date(tarea.getFecha().getTime()));
                stmtInsert.setString(4, tarea.getEstado());
                stmtInsert.setInt(5, tarea.getUsuarioId());
                stmtInsert.executeUpdate();

                // Obtener el ID generado
                ResultSet rs = stmtInsert.getGeneratedKeys();
                if (rs.next()) {
                    tarea.setId(rs.getInt(1));
                }
            }

            // Reactivar las restricciones
            try (Statement stmtPragma2 = conn.createStatement()) {
                stmtPragma2.execute("PRAGMA ignore_check_constraints = OFF;");
            }
        } finally {
            // No cerramos la conexión aquí, ya que es manejada por el pool de conexiones
            // o se cierra automáticamente al usar try-with-resources
        }
    }

    /**
     * Obtiene tareas de un usuario específico
     */
    public List<tarea> obtenerPorUsuario(Integer usuarioId) throws SQLException {
        String sql = "SELECT * FROM tarea WHERE usuario_id = ? ORDER BY fecha DESC";
        List<tarea> tareas = new ArrayList<>();
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tareas.add(extraerTarea(rs));
            }
        }
        return tareas;
    }

    private tarea extraerTarea(ResultSet rs) throws SQLException {
        tarea t = new tarea();
        t.setId(rs.getInt("id"));
        t.setConcepto(rs.getString("concepto"));
        t.setCantidad(rs.getDouble("cantidad"));

        // Extraer fecha de forma segura
        try {
            t.setFecha(rs.getTimestamp("fecha"));
        } catch (SQLException e) {
            // Si hay error con timestamp, intentar con date
            try {
                t.setFecha(rs.getDate("fecha"));
            } catch (SQLException ex) {
                // Si falla, usar fecha actual
                t.setFecha(new java.util.Date());
            }
        }

        t.setEstado(rs.getString("estado"));
        t.setUsuarioId(rs.getInt("usuario_id"));
        return t;
    }

    @Override
    public void actualizar(tarea tarea) throws SQLException {
        String sql = "UPDATE tarea SET concepto = ?, cantidad = ?, fecha = ?, estado = ? WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setString(1, tarea.getConcepto());
            stmt.setDouble(2, tarea.getCantidad());
            stmt.setDate(3, new java.sql.Date(tarea.getFecha().getTime()));
            stmt.setString(4, tarea.getEstado());
            stmt.setInt(5, tarea.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Actualiza una tarea existente sin activar los triggers de la base de datos
     */
    public void actualizarSinTrigger(tarea tarea) throws SQLException {
        Connection conn = null;
        try {
            conn = conectar();
            
            // Desactivar temporalmente las restricciones
            try (Statement stmtPragma1 = conn.createStatement()) {
                stmtPragma1.execute("PRAGMA ignore_check_constraints = ON;");
            }
            
            // Ejecutar la actualización
            String sqlUpdate = "UPDATE tarea SET concepto = ?, cantidad = ?, fecha = ?, estado = ? WHERE id = ?";
            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                stmtUpdate.setString(1, tarea.getConcepto());
                stmtUpdate.setDouble(2, tarea.getCantidad());
                stmtUpdate.setDate(3, new java.sql.Date(tarea.getFecha().getTime()));
                stmtUpdate.setString(4, tarea.getEstado());
                stmtUpdate.setInt(5, tarea.getId());
                stmtUpdate.executeUpdate();
            }
            
            // Reactivar las restricciones
            try (Statement stmtPragma2 = conn.createStatement()) {
                stmtPragma2.execute("PRAGMA ignore_check_constraints = OFF;");
            }
        } finally {
            // No cerramos la conexión aquí, ya que es manejada por el pool de conexiones
            // o se cierra automáticamente al usar try-with-resources
        }
    }

    @Override
    public void eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM tarea WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Elimina una tarea existente sin activar los triggers de la base de datos
     */
    public void eliminarSinTrigger(Integer id) throws SQLException {
        Connection conn = null;
        try {
            conn = conectar();

            // Desactivar temporalmente las restricciones
            try (Statement stmtPragma1 = conn.createStatement()) {
                stmtPragma1.execute("PRAGMA ignore_check_constraints = ON;");
            }

            // Ejecutar la eliminación
            try (PreparedStatement stmtDelete = conn.prepareStatement("DELETE FROM tarea WHERE id = ?")) {
                stmtDelete.setInt(1, id);
                stmtDelete.executeUpdate();
            }

            // Reactivar las restricciones
            try (Statement stmtPragma2 = conn.createStatement()) {
                stmtPragma2.execute("PRAGMA ignore_check_constraints = OFF;");
            }
        } finally {
            // No cerramos la conexión aquí, ya que es manejada por el pool de conexiones
            // o se cierra automáticamente al usar try-with-resources
        }
    }

    @Override
    public tarea obtenerPorId(Integer id) throws SQLException {
        String sql = "SELECT * FROM tarea WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extraerTarea(rs);
            }
        }
        return null;
    }

    @Override
    public List<tarea> obtenerTodos() throws SQLException {
        List<tarea> tareas = new ArrayList<>();
        String sql = "SELECT * FROM tarea";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tareas.add(extraerTarea(rs));
            }
        }
        return tareas;
    }

    /**
     * Calcula el total por tipo y período
     */
    public double calcularTotalPorTipoYPeriodo(Integer usuarioId, String estado, Date inicio, Date fin)
            throws SQLException {
        String sql = "SELECT SUM(cantidad) as total FROM tarea WHERE usuario_id = ? AND estado = ? AND fecha BETWEEN ? AND ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, estado);
            stmt.setTimestamp(3, new Timestamp(inicio.getTime()));
            stmt.setTimestamp(4, new Timestamp(fin.getTime()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    /**
     * Obtiene tareas por usuario y período
     */
    public List<tarea> obtenerPorUsuarioYPeriodo(Integer usuarioId, Date inicio, Date fin) throws SQLException {
        String sql = "SELECT * FROM tarea WHERE usuario_id = ? AND fecha BETWEEN ? AND ? ORDER BY fecha DESC";
        List<tarea> tareas = new ArrayList<>();
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setTimestamp(2, new Timestamp(inicio.getTime()));
            stmt.setTimestamp(3, new Timestamp(fin.getTime()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tareas.add(extraerTarea(rs));
            }
        }
        return tareas;
    }

    /**
     * Obtiene las últimas N tareas de un usuario
     */
    public List<tarea> obtenerUltimasTareas(Integer usuarioId, int limite) throws SQLException {
        String sql = "SELECT * FROM tarea WHERE usuario_id = ? ORDER BY fecha DESC LIMIT ?";
        List<tarea> tareas = new ArrayList<>();

        try (Connection conn = conectar();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, limite);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                try {
                    tareas.add(extraerTarea(rs));
                } catch (Exception e) {
                    System.err.println("Error extrayendo tarea del ResultSet: " + e.getMessage());
                    // Continuar con la siguiente tarea
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en consulta de tareas: " + e.getMessage());
            throw e;
        }

        return tareas;
    }

    /**
     * Actualiza las estadísticas del usuario basándose en sus tareas
     */
    public void actualizarEstadisticasUsuario(Integer usuarioId) throws SQLException {
        // Calcular totales
        double totalIngresos = calcularTotalPorTipo(usuarioId, tarea.ESTADO_INGRESO);
        double totalGastos = calcularTotalPorTipo(usuarioId, tarea.ESTADO_GASTO);

        // Obtener la estadística del usuario o crear una nueva
        EstadisticaDAO estadisticaDAO = new EstadisticaDAO();
        estadistica stats = estadisticaDAO.obtenerPorUsuario(usuarioId);

        if (stats == null) {
            stats = new estadistica(totalIngresos, totalGastos, usuarioId);
            estadisticaDAO.insertar(stats);
        } else {
            stats.setTotalIngreso(totalIngresos);
            stats.setTotalGasto(totalGastos);
            estadisticaDAO.actualizar(stats);
        }
    }

    /**
     * Calcula el total por tipo para todas las tareas de un usuario
     */
    private double calcularTotalPorTipo(Integer usuarioId, String estado) throws SQLException {
        String sql = "SELECT SUM(cantidad) as total FROM tarea WHERE usuario_id = ? AND estado = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setString(2, estado);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble("total");
                return rs.wasNull() ? 0 : total;
            }
        }
        return 0.0;
    }
}
