package es.franciscorodalf.saveinvestor.backend.dao;

import es.franciscorodalf.saveinvestor.backend.model.CuentaInversion;
import es.franciscorodalf.saveinvestor.backend.model.CuentaInversion.TipoActivo;
import es.franciscorodalf.saveinvestor.backend.model.abstractas.Conexion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CuentaInversionDAO extends Conexion implements DAO<CuentaInversion> {

    private CuentaInversion mapearCuenta(ResultSet rs) throws SQLException {
        CuentaInversion cuenta = new CuentaInversion();
        cuenta.setId(rs.getInt("id"));
        cuenta.setUsuarioId(rs.getInt("usuario_id"));
        cuenta.setNombre(rs.getString("nombre"));
        cuenta.setTipoActivo(TipoActivo.fromDatabaseValue(rs.getString("tipo_activo")));
        cuenta.setMoneda(rs.getString("moneda"));
        cuenta.setValorInicial(rs.getDouble("valor_inicial"));
        cuenta.setValorActual(rs.getDouble("valor_actual"));
        Timestamp timestamp = rs.getTimestamp("fecha_creacion");
        if (timestamp != null) {
            cuenta.setFechaCreacion(timestamp.toLocalDateTime());
        }
        cuenta.setDescripcion(rs.getString("descripcion"));
        return cuenta;
    }

    @Override
    public void insertar(CuentaInversion cuenta) throws SQLException {
        String sql = "INSERT INTO cuenta_inversion (usuario_id, nombre, tipo_activo, moneda, valor_inicial, valor_actual, fecha_creacion, descripcion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, cuenta.getUsuarioId());
            stmt.setString(2, cuenta.getNombre());
            stmt.setString(3, cuenta.getTipoActivo().toDatabaseValue());
            stmt.setString(4, cuenta.getMoneda());
            stmt.setDouble(5, cuenta.getValorInicial() != null ? cuenta.getValorInicial() : 0);
            stmt.setDouble(6, cuenta.getValorActual() != null ? cuenta.getValorActual() : 0);
            if (cuenta.getFechaCreacion() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(cuenta.getFechaCreacion()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }
            stmt.setString(8, cuenta.getDescripcion());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    cuenta.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void actualizar(CuentaInversion cuenta) throws SQLException {
        String sql = "UPDATE cuenta_inversion SET usuario_id = ?, nombre = ?, tipo_activo = ?, moneda = ?, valor_inicial = ?, valor_actual = ?, fecha_creacion = ?, descripcion = ? WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, cuenta.getUsuarioId());
            stmt.setString(2, cuenta.getNombre());
            stmt.setString(3, cuenta.getTipoActivo().toDatabaseValue());
            stmt.setString(4, cuenta.getMoneda());
            stmt.setDouble(5, cuenta.getValorInicial() != null ? cuenta.getValorInicial() : 0);
            stmt.setDouble(6, cuenta.getValorActual() != null ? cuenta.getValorActual() : 0);
            if (cuenta.getFechaCreacion() != null) {
                stmt.setTimestamp(7, Timestamp.valueOf(cuenta.getFechaCreacion()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }
            stmt.setString(8, cuenta.getDescripcion());
            stmt.setInt(9, cuenta.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM cuenta_inversion WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public CuentaInversion obtenerPorId(Integer id) throws SQLException {
        String sql = "SELECT * FROM cuenta_inversion WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearCuenta(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<CuentaInversion> obtenerTodos() throws SQLException {
        String sql = "SELECT * FROM cuenta_inversion";
        List<CuentaInversion> cuentas = new ArrayList<>();
        try (PreparedStatement stmt = conectar().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cuentas.add(mapearCuenta(rs));
            }
        }
        return cuentas;
    }

    public List<CuentaInversion> obtenerPorUsuario(Integer usuarioId) throws SQLException {
        String sql = "SELECT * FROM cuenta_inversion WHERE usuario_id = ? ORDER BY fecha_creacion";
        List<CuentaInversion> cuentas = new ArrayList<>();
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cuentas.add(mapearCuenta(rs));
                }
            }
        }
        return cuentas;
    }

    public void actualizarValorActual(Integer cuentaId, double nuevoValor) throws SQLException {
        String sql = "UPDATE cuenta_inversion SET valor_actual = ?, fecha_creacion = COALESCE(fecha_creacion, ?) WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setDouble(1, nuevoValor);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, cuentaId);
            stmt.executeUpdate();
        }
    }
}
