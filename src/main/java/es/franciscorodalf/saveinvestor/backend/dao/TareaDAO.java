package es.franciscorodalf.saveinvestor.backend.dao;

import es.franciscorodalf.saveinvestor.backend.model.tarea;
import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;
import java.sql.*;
import java.util.ArrayList;
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

    public List<tarea> obtenerPorUsuario(Integer usuarioId) throws SQLException {
        List<tarea> tareas = new ArrayList<>();
        String sql = "SELECT * FROM tarea WHERE usuario_id = ?";
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
        t.setFecha(rs.getDate("fecha"));
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

    @Override
    public void eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM tarea WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
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
    
}
