package es.franciscorodalf.saveinvestor.backend.dao;

import es.franciscorodalf.saveinvestor.backend.model.Usuario;
import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO extends Conexion implements DAO<Usuario> {
    
    @Override
    public void insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, email, contrasenia) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getContrasenia());
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                usuario.setId(rs.getInt(1));
            }
        }
    }

    public Usuario autenticar(String usuarioOEmail, String contrasenia) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE (email = ? OR nombre = ?) AND contrasenia = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setString(1, usuarioOEmail);
            stmt.setString(2, usuarioOEmail);
            stmt.setString(3, contrasenia);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extraerUsuario(rs);
            }
        }
        return null;
    }

    private Usuario extraerUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setEmail(rs.getString("email"));
        usuario.setContrasenia(rs.getString("contrasenia"));
        return usuario;
    }
    
    @Override
    public void actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nombre = ?, email = ?, contrasenia = ? WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getContrasenia());
            stmt.setInt(4, usuario.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Elimina un usuario y todos sus datos relacionados.
     * Este método gestiona la eliminación en cascada de todas las entidades 
     * relacionadas con el usuario (tareas, estadísticas, objetivos).
     * 
     * @param id ID del usuario a eliminar
     * @return true si la eliminación fue exitosa
     * @throws SQLException si ocurre un error durante la eliminación
     */
    public boolean eliminarCompleto(Integer id) throws SQLException {
        Connection conn = null;
        boolean exito = false;
        
        try {
            conn = conectar();
            conn.setAutoCommit(false);
            
            // 1. Eliminar tareas del usuario
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM tarea WHERE usuario_id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            
            // 2. Eliminar objetivos del usuario
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM objetivo WHERE usuario_id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            
            // 3. Eliminar estadísticas del usuario
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM estadistica WHERE usuario_id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            
            // 4. Finalmente, eliminar el usuario
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM usuario WHERE id = ?")) {
                stmt.setInt(1, id);
                int filasAfectadas = stmt.executeUpdate();
                exito = (filasAfectadas > 0);
            }
            
            // Si todo salió bien, confirmar los cambios
            conn.commit();
            
        } catch (SQLException e) {
            // Si hay error, hacer rollback
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error al hacer rollback: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            // Restaurar autocommit y cerrar conexión
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    System.err.println("Error al restaurar autocommit: " + ex.getMessage());
                }
            }
        }
        
        return exito;
    }

    @Override
    public Usuario obtenerPorId(Integer id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extraerUsuario(rs);
            }
        }
        return null;
    }

    @Override
    public List<Usuario> obtenerTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuario";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                usuarios.add(extraerUsuario(rs));
            }
        }
        return usuarios;
    }
}
