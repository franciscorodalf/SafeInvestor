package es.franciscorodalf.saveinvestor.backend.dao;

import es.franciscorodalf.saveinvestor.backend.model.estadistica;
import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class EstadisticaDAO extends Conexion implements DAO<estadistica> {

    @Override
    public void insertar(estadistica estadistica) throws SQLException {
        String sql = "INSERT INTO estadistica (total_ingreso, total_gasto, usuario_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setDouble(1, estadistica.getTotalIngreso());
            stmt.setDouble(2, estadistica.getTotalGasto());
            stmt.setInt(3, estadistica.getUsuarioId());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                estadistica.setId(rs.getInt(1));
            }
        }
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

    private estadistica extraerEstadistica(ResultSet rs) throws SQLException {
        estadistica e = new estadistica();
        e.setId(rs.getInt("id"));
        e.setTotalIngreso(rs.getDouble("total_ingreso"));
        e.setTotalGasto(rs.getDouble("total_gasto"));
        e.setUsuarioId(rs.getInt("usuario_id"));
        return e;
    }

    @Override
    public void actualizar(estadistica estadistica) throws SQLException {
        String sql = "UPDATE estadistica SET total_ingreso = ?, total_gasto = ? WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setDouble(1, estadistica.getTotalIngreso());
            stmt.setDouble(2, estadistica.getTotalGasto());
            stmt.setInt(3, estadistica.getId());
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

    @Override
    public List<estadistica> obtenerTodos() throws SQLException {
        String sql = "SELECT * FROM estadistica";
        List<estadistica> estadisticas = new ArrayList<>();
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                estadisticas.add(extraerEstadistica(rs));
            }
        }
        return estadisticas;
    }
}
