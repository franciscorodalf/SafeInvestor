package es.franciscorodalf.saveinvestor.backend.model;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;



public class UsuarioServiceModel extends Conexion {

    public UsuarioServiceModel() {
    }

    public UsuarioServiceModel(String unaRutaArchivoBD) throws SQLException {
        super(unaRutaArchivoBD);
    }

    public UsuarioEntity obtenerUsuarioPorNombre(String nombre) {
        try {
            String sql = "SELECT * FROM Usuario " + "where nombre='" + nombre + "'";
            ArrayList<UsuarioEntity> usuarios = obtenerUsuario(sql);
            if (usuarios.isEmpty()) {
                return null;
            }
            return usuarios.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public UsuarioEntity obtenerDatosUsuario(String informacion) {
        try {
            String sql = "SELECT * FROM Usuario " + "where email='" + informacion + "'";
            ArrayList<UsuarioEntity> usuarios = obtenerUsuario(sql);
            sql = "SELECT * FROM Usuario " + "where nombre='" + informacion + "'";
            usuarios = obtenerUsuario(sql);

            if (usuarios.isEmpty()) {
                return null;
            }

            return usuarios.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public UsuarioEntity obtenerUsuarioPorEmail(String email) {
        try {
            String sql = "SELECT * FROM Usuario " + "where email='" + email + "'";
            ArrayList<UsuarioEntity> usuarios = obtenerUsuario(sql);
            if (usuarios.isEmpty()) {
                return null;
            }
            return usuarios.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public ArrayList<UsuarioEntity> obtenerUsuarios() throws SQLException {
        String sql = "SELECT * FROM Usuario";
        return obtenerUsuario(sql);
    }

    public ArrayList<UsuarioEntity> obtenerUsuario(String sql) throws SQLException {
        ArrayList<UsuarioEntity> usuarios = new ArrayList<UsuarioEntity>();
        try {
            PreparedStatement sentencia = getConnection().prepareStatement(sql);
            ResultSet resultado = sentencia.executeQuery();

            while (resultado.next()) {
                String nombreStr = resultado.getString("nombre");
                String contraseniaStr = resultado.getString("contrasenia");
                String emailStr = resultado.getString("email");
                UsuarioEntity usuarioModel = new UsuarioEntity(emailStr, nombreStr, contraseniaStr);
                usuarios.add(usuarioModel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cerrar();
        }
        return usuarios;
    }

    public boolean agregarUsuario(UsuarioEntity usuario) throws SQLException {
        if (usuario == null) {
            return false;
        }
        ArrayList<UsuarioEntity> usuarios = obtenerUsuarios();
        String sql = "INSERT  INTO usuario (nombre,email,contrasenia) VALUES ('" + usuario.getNombre() + "', '"
                + usuario.getEmail() + "', '" + usuario.getContrasenia() + "')";

        if (usuarios.contains(usuario)) {
            return false;
        }

        try {
            PreparedStatement sentencia = getConnection().prepareStatement(sql);
            sentencia.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cerrar();
        }
        return true;
    }

}
