package es.franciscorodalf.saveinvestor.backend.dao;

import es.franciscorodalf.saveinvestor.backend.model.MovimientoInversion;
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

public class MovimientoInversionDAO extends Conexion implements DAO<MovimientoInversion> {

    private MovimientoInversion mapearMovimiento(ResultSet rs) throws SQLException {
        MovimientoInversion movimiento = new MovimientoInversion();
        movimiento.setId(rs.getInt("id"));
        movimiento.setCuentaId(rs.getInt("cuenta_id"));
        Timestamp timestamp = rs.getTimestamp("fecha");
        if (timestamp != null) {
            movimiento.setFecha(timestamp.toLocalDateTime());
        }
        movimiento.setAporte(rs.getDouble("aporte"));
        movimiento.setRetiro(rs.getDouble("retiro"));
        movimiento.setValorTotal(rs.getDouble("valor_total"));
        movimiento.setRentabilidad(rs.getDouble("rentabilidad"));
        movimiento.setNotas(rs.getString("notas"));
        return movimiento;
    }

    @Override
    public void insertar(MovimientoInversion movimiento) throws SQLException {
        String sql = "INSERT INTO movimiento_inversion (cuenta_id, fecha, aporte, retiro, valor_total, rentabilidad, notas) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conectar().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, movimiento.getCuentaId());
            if (movimiento.getFecha() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(movimiento.getFecha()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            stmt.setDouble(3, movimiento.getAporte() != null ? movimiento.getAporte() : 0);
            stmt.setDouble(4, movimiento.getRetiro() != null ? movimiento.getRetiro() : 0);
            stmt.setDouble(5, movimiento.getValorTotal());
            stmt.setDouble(6, movimiento.getRentabilidad() != null ? movimiento.getRentabilidad() : 0);
            stmt.setString(7, movimiento.getNotas());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    movimiento.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void actualizar(MovimientoInversion movimiento) throws SQLException {
        String sql = "UPDATE movimiento_inversion SET cuenta_id = ?, fecha = ?, aporte = ?, retiro = ?, valor_total = ?, rentabilidad = ?, notas = ? WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, movimiento.getCuentaId());
            if (movimiento.getFecha() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(movimiento.getFecha()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            stmt.setDouble(3, movimiento.getAporte() != null ? movimiento.getAporte() : 0);
            stmt.setDouble(4, movimiento.getRetiro() != null ? movimiento.getRetiro() : 0);
            stmt.setDouble(5, movimiento.getValorTotal());
            stmt.setDouble(6, movimiento.getRentabilidad() != null ? movimiento.getRentabilidad() : 0);
            stmt.setString(7, movimiento.getNotas());
            stmt.setInt(8, movimiento.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void eliminar(Integer id) throws SQLException {
        String sql = "DELETE FROM movimiento_inversion WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public MovimientoInversion obtenerPorId(Integer id) throws SQLException {
        String sql = "SELECT * FROM movimiento_inversion WHERE id = ?";
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearMovimiento(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<MovimientoInversion> obtenerTodos() throws SQLException {
        String sql = "SELECT * FROM movimiento_inversion";
        List<MovimientoInversion> movimientos = new ArrayList<>();
        try (PreparedStatement stmt = conectar().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                movimientos.add(mapearMovimiento(rs));
            }
        }
        return movimientos;
    }

    public List<MovimientoInversion> obtenerPorCuenta(Integer cuentaId) throws SQLException {
        String sql = "SELECT * FROM movimiento_inversion WHERE cuenta_id = ? ORDER BY fecha";
        List<MovimientoInversion> movimientos = new ArrayList<>();
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, cuentaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearMovimiento(rs));
                }
            }
        }
        return movimientos;
    }

    public List<MovimientoInversion> obtenerPorUsuario(Integer usuarioId) throws SQLException {
        String sql = "SELECT mi.* FROM movimiento_inversion mi JOIN cuenta_inversion ci ON mi.cuenta_id = ci.id WHERE ci.usuario_id = ? ORDER BY mi.fecha";
        List<MovimientoInversion> movimientos = new ArrayList<>();
        try (PreparedStatement stmt = conectar().prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapearMovimiento(rs));
                }
            }
        }
        return movimientos;
    }
}
