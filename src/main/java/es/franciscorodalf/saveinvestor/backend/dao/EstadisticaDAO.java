package es.franciscorodalf.saveinvestor.backend.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;
import es.franciscorodalf.saveinvestor.backend.model.estadistica;

public class EstadisticaDAO extends Conexion implements DAO<estadistica> {

    @Override
    public void insertar(estadistica stats) throws SQLException {
        String sql = "INSERT INTO estadistica (total_ingreso, total_gasto, usuario_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, stats.getTotalIngreso());
            stmt.setDouble(2, stats.getTotalGasto());
            stmt.setInt(3, stats.getUsuarioId());
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                stats.setId(rs.getInt(1));
            }
        }
    }

    @Override
    public void actualizar(estadistica stats) throws SQLException {
        String sql = "UPDATE estadistica SET total_ingreso = ?, total_gasto = ? WHERE usuario_id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setDouble(1, stats.getTotalIngreso());
            stmt.setDouble(2, stats.getTotalGasto());
            stmt.setInt(3, stats.getUsuarioId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM estadistica WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public estadistica obtenerPorId(Integer id) throws SQLException {
        String sql = "SELECT * FROM estadistica WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extraerEstadistica(rs);
            }
        }
        return null;
    }

    public estadistica obtenerPorUsuario(Integer usuarioId) throws SQLException {
        String sql = "SELECT * FROM estadistica WHERE usuario_id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extraerEstadistica(rs);
            }
        }
        return null;
    }

    @Override
    public List<estadistica> obtenerTodos() throws SQLException {
        List<estadistica> estadisticas = new ArrayList<>();
        String sql = "SELECT * FROM estadistica";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                estadisticas.add(extraerEstadistica(rs));
            }
        }
        return estadisticas;
    }
    
    /**
     * Incrementa el total de ingresos para un usuario
     */
    public void incrementarIngresos(Integer usuarioId, double cantidad) throws SQLException {
        estadistica stats = obtenerPorUsuario(usuarioId);
        if (stats != null) {
            stats.setTotalIngreso(stats.getTotalIngreso() + cantidad);
            actualizar(stats);
        } else {
            estadistica nuevaEstadistica = new estadistica(cantidad, 0.0, usuarioId);
            insertar(nuevaEstadistica);
        }
    }
    
    /**
     * Incrementa el total de gastos para un usuario
     */
    public void incrementarGastos(Integer usuarioId, double cantidad) throws SQLException {
        estadistica stats = obtenerPorUsuario(usuarioId);
        if (stats != null) {
            stats.setTotalGasto(stats.getTotalGasto() + cantidad);
            actualizar(stats);
        } else {
            estadistica nuevaEstadistica = new estadistica(0.0, cantidad, usuarioId);
            insertar(nuevaEstadistica);
        }
    }

    private estadistica extraerEstadistica(ResultSet rs) throws SQLException {
        estadistica stats = new estadistica();
        stats.setId(rs.getInt("id"));
        stats.setTotalIngreso(rs.getDouble("total_ingreso"));
        stats.setTotalGasto(rs.getDouble("total_gasto"));
        stats.setUsuarioId(rs.getInt("usuario_id"));
        return stats;
    }
}
