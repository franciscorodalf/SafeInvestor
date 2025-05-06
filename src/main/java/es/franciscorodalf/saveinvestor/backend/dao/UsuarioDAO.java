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

/**
 * 
 * 
 * package es.ies.puerto.modelo.db.services;

import es.ies.puerto.modelo.db.entidades.Videojuego;
import es.ies.puerto.modelo.db.entidades.comun.Conexion;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VideojuegoService extends Conexion {

    public VideojuegoService() { super(); }

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private Videojuego leerVideojuego(ResultSet rs) throws Exception {
        int id = rs.getInt("id");
        String nombre = rs.getString("nombre");
        String fechaStr = rs.getString("fecha_lanzamiento");
        Date fecha = sdf.parse(fechaStr);
        return new Videojuego(id, nombre, fecha);
    }

    // ✅ CREATE
    public boolean insertarVideojuego(Videojuego videojuego, String nombreCategoria) {
        try {
            // Insertar videojuego
            String sql = "INSERT INTO videojuegos (id, nombre, fecha_lanzamiento) VALUES (?, ?, ?)";
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setInt(1, videojuego.getId());
            stmt.setString(2, videojuego.getNombre());
            stmt.setString(3, sdf.format(videojuego.getFechaLanzamiento()));
            stmt.executeUpdate();
            stmt.close();

            // Buscar id de la categoría
            String sqlCat = "SELECT id FROM categoria WHERE nombre = ?";
            PreparedStatement stmtCat = getConnection().prepareStatement(sqlCat);
            stmtCat.setString(1, nombreCategoria);
            ResultSet rs = stmtCat.executeQuery();
            int idCategoria = rs.next() ? rs.getInt("id") : -1;
            rs.close(); stmtCat.close();

            if (idCategoria == -1) return false;

            // Insertar relación
            String sqlRel = "INSERT INTO videojuego_categoria (videojuego_id, categoria_id) VALUES (?, ?)";
            PreparedStatement stmtRel = getConnection().prepareStatement(sqlRel);
            stmtRel.setInt(1, videojuego.getId());
            stmtRel.setInt(2, idCategoria);
            stmtRel.executeUpdate();
            stmtRel.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            cerrar();
        }
    }

    // ✅ READ (Todos)
    public List<Videojuego> obtenerTodos() {
        List<Videojuego> lista = new ArrayList<>();
        String sql = "SELECT * FROM videojuegos";

        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(leerVideojuego(rs));
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cerrar();
        }

        return lista;
    }

    // ✅ READ por categoría
    public List<Videojuego> obtenerPorCategoria(String categoria) {
        List<Videojuego> lista = new ArrayList<>();

        String sql = "SELECT videojuegos.id, videojuegos.nombre, videojuegos.fecha_lanzamiento " +
                "FROM videojuegos " +
                "JOIN videojuego_categoria ON videojuegos.id = videojuego_categoria.videojuego_id " +
                "JOIN categoria ON categoria.id = videojuego_categoria.categoria_id " +
                "WHERE categoria.nombre = ?";

        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setString(1, categoria);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(leerVideojuego(rs));
            }

            rs.close(); stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cerrar();
        }

        return lista;
    }

    // ✅ UPDATE
    public boolean actualizarVideojuego(Videojuego videojuego) {
        String sql = "UPDATE videojuegos SET nombre = ?, fecha_lanzamiento = ? WHERE id = ?";
        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setString(1, videojuego.getNombre());
            stmt.setString(2, sdf.format(videojuego.getFechaLanzamiento()));
            stmt.setInt(3, videojuego.getId());
            stmt.executeUpdate();
            stmt.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            cerrar();
        }
    }

    // ✅ DELETE
    public boolean eliminarVideojuego(int id) {
        try {
            // Eliminar relación
            String rel = "DELETE FROM videojuego_categoria WHERE videojuego_id = ?";
            PreparedStatement stmtRel = getConnection().prepareStatement(rel);
            stmtRel.setInt(1, id);
            stmtRel.executeUpdate();
            stmtRel.close();

            // Eliminar videojuego
            String sql = "DELETE FROM videojuegos WHERE id = ?";
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.executeUpdate();
            stmt.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            cerrar();
        }
    }

    // ✅ Buscar videojuegos lanzados después de una fecha
    public List<Videojuego> lanzadosDespues(Date fecha) {
        List<Videojuego> lista = new ArrayList<>();
        String sql = "SELECT * FROM videojuegos WHERE fecha_lanzamiento > ?";

        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setString(1, sdf.format(fecha));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(leerVideojuego(rs));
            }

            rs.close(); stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cerrar();
        }

        return lista;
    }

    // ✅ Buscar videojuegos entre dos fechas
    public List<Videojuego> entreFechas(Date inicio, Date fin) {
        List<Videojuego> lista = new ArrayList<>();
        String sql = "SELECT * FROM videojuegos WHERE fecha_lanzamiento BETWEEN ? AND ?";

        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setString(1, sdf.format(inicio));
            stmt.setString(2, sdf.format(fin));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(leerVideojuego(rs));
            }

            rs.close(); stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cerrar();
        }

        return lista;
    }
}

 */