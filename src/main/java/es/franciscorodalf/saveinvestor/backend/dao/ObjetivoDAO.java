package es.franciscorodalf.saveinvestor.backend.dao;

import es.franciscorodalf.saveinvestor.backend.model.Objetivo;
import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ObjetivoDAO extends Conexion implements DAO<Objetivo> {

    @Override
    public void insertar(Objetivo objetivo) throws SQLException {
        String sql = "INSERT INTO objetivo (descripcion, cantidad_objetivo, cantidad_actual, fecha_objetivo, usuario_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, objetivo.getDescripcion());
            stmt.setDouble(2, objetivo.getCantidadObjetivo());
            stmt.setDouble(3, objetivo.getCantidadActual());
            
            if (objetivo.getFechaObjetivo() != null) {
                stmt.setDate(4, new java.sql.Date(objetivo.getFechaObjetivo().getTime()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            
            stmt.setInt(5, objetivo.getUsuarioId());
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                objetivo.setId(rs.getInt(1));
            }
        }
    }
    
    @Override
    public void actualizar(Objetivo objetivo) throws SQLException {
        String sql = "UPDATE objetivo SET descripcion = ?, cantidad_objetivo = ?, " +
                     "cantidad_actual = ?, fecha_objetivo = ?, completado = ? " +
                     "WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setString(1, objetivo.getDescripcion());
            stmt.setDouble(2, objetivo.getCantidadObjetivo());
            stmt.setDouble(3, objetivo.getCantidadActual());
            
            if (objetivo.getFechaObjetivo() != null) {
                stmt.setDate(4, new java.sql.Date(objetivo.getFechaObjetivo().getTime()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }
            
            stmt.setInt(5, objetivo.isCompletado() ? 1 : 0);
            stmt.setInt(6, objetivo.getId());
            stmt.executeUpdate();
        }
    }
    
    @Override
    public void eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM objetivo WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    @Override
    public Objetivo obtenerPorId(Integer id) throws SQLException {
        String sql = "SELECT * FROM objetivo WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extraerObjetivo(rs);
            }
        }
        return null;
    }
    
    @Override
    public List<Objetivo> obtenerTodos() throws SQLException {
        List<Objetivo> objetivos = new ArrayList<>();
        String sql = "SELECT * FROM objetivo ORDER BY completado, fecha_objetivo";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                objetivos.add(extraerObjetivo(rs));
            }
        }
        return objetivos;
    }
    
    /**
     * Obtiene todos los objetivos de un usuario específico
     * @param usuarioId ID del usuario
     * @return Lista de objetivos del usuario
     */
    public List<Objetivo> obtenerPorUsuario(Integer usuarioId) throws SQLException {
        List<Objetivo> objetivos = new ArrayList<>();
        String sql = "SELECT * FROM objetivo WHERE usuario_id = ? ORDER BY completado, fecha_objetivo";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                objetivos.add(extraerObjetivo(rs));
            }
        }
        return objetivos;
    }
    
    /**
     * Obtiene los objetivos activos (no completados) de un usuario
     */
    public List<Objetivo> obtenerObjetivosActivos(Integer usuarioId) throws SQLException {
        List<Objetivo> objetivos = new ArrayList<>();
        String sql = "SELECT * FROM objetivo WHERE usuario_id = ? AND completado = 0 ORDER BY fecha_objetivo";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                objetivos.add(extraerObjetivo(rs));
            }
        }
        return objetivos;
    }
    
    /**
     * Contribuye una cantidad a un objetivo específico
     */
    public boolean contribuirAObjetivo(Integer objetivoId, Double cantidad) throws SQLException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        
        // 1. Obtener el objetivo
        Objetivo objetivo = obtenerPorId(objetivoId);
        if (objetivo == null) {
            throw new IllegalArgumentException("Objetivo no encontrado");
        }
        
        // 2. Añadir la cantidad y verificar si se completó
        boolean seCompleto = objetivo.contribuir(cantidad);
        
        // 3. Actualizar en base de datos
        actualizar(objetivo);
        
        return seCompleto;
    }
    
    /**
     * Extrae un objeto Objetivo a partir de un ResultSet
     */
    private Objetivo extraerObjetivo(ResultSet rs) throws SQLException {
        Objetivo objetivo = new Objetivo();
        objetivo.setId(rs.getInt("id"));
        objetivo.setDescripcion(rs.getString("descripcion"));
        objetivo.setCantidadObjetivo(rs.getDouble("cantidad_objetivo"));
        objetivo.setCantidadActual(rs.getDouble("cantidad_actual"));
        objetivo.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        
        // La fecha objetivo puede ser nula
        java.sql.Date fechaObjetivo = rs.getDate("fecha_objetivo");
        if (fechaObjetivo != null) {
            objetivo.setFechaObjetivo(new java.util.Date(fechaObjetivo.getTime()));
        }
        
        objetivo.setCompletado(rs.getInt("completado") == 1);
        objetivo.setUsuarioId(rs.getInt("usuario_id"));
        
        return objetivo;
    }
}
