package es.franciscorodalf.saveinvestor.backend.service;

import es.franciscorodalf.saveinvestor.backend.dao.CuentaInversionDAO;
import es.franciscorodalf.saveinvestor.backend.dao.MovimientoInversionDAO;
import es.franciscorodalf.saveinvestor.backend.model.CuentaInversion;
import es.franciscorodalf.saveinvestor.backend.model.CuentaInversion.TipoActivo;
import es.franciscorodalf.saveinvestor.backend.model.MovimientoInversion;
import es.franciscorodalf.saveinvestor.backend.model.ValorHistoricoInversion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PortafolioService {

    private final CuentaInversionDAO cuentaDAO;
    private final MovimientoInversionDAO movimientoDAO;

    public PortafolioService() {
        this(new CuentaInversionDAO(), new MovimientoInversionDAO());
    }

    public PortafolioService(CuentaInversionDAO cuentaDAO, MovimientoInversionDAO movimientoDAO) {
        this.cuentaDAO = cuentaDAO;
        this.movimientoDAO = movimientoDAO;
    }

    public Map<TipoActivo, Double> calcularDistribucionPorTipo(Integer usuarioId) throws SQLException {
        List<CuentaInversion> cuentas = cuentaDAO.obtenerPorUsuario(usuarioId);
        Map<TipoActivo, Double> distribucion = new EnumMap<>(TipoActivo.class);
        for (CuentaInversion cuenta : cuentas) {
            double valor = cuenta.getValorActual() != null ? cuenta.getValorActual() : 0d;
            distribucion.merge(cuenta.getTipoActivo(), valor, Double::sum);
        }
        return distribucion;
    }

    public double obtenerValorActualTotal(Integer usuarioId) throws SQLException {
        List<CuentaInversion> cuentas = cuentaDAO.obtenerPorUsuario(usuarioId);
        return cuentas.stream()
                .map(CuentaInversion::getValorActual)
                .filter(valor -> valor != null)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public List<ValorHistoricoInversion> obtenerRendimientoHistorico(Integer usuarioId) throws SQLException {
        List<MovimientoInversion> movimientos = new ArrayList<>(movimientoDAO.obtenerPorUsuario(usuarioId));
        movimientos.sort(Comparator.comparing(movimiento -> {
            if (movimiento.getFecha() == null) {
                return LocalDate.now().atStartOfDay();
            }
            return movimiento.getFecha();
        }));

        Map<Integer, Double> ultimoValorPorCuenta = new HashMap<>();
        Map<LocalDate, Double> valorPorFecha = new LinkedHashMap<>();

        for (MovimientoInversion movimiento : movimientos) {
            LocalDate fecha = movimiento.getFecha() != null ? movimiento.getFecha().toLocalDate() : LocalDate.now();
            ultimoValorPorCuenta.put(movimiento.getCuentaId(), movimiento.getValorTotal());
            double total = ultimoValorPorCuenta.values().stream().mapToDouble(Double::doubleValue).sum();
            valorPorFecha.put(fecha, total);
        }

        if (valorPorFecha.isEmpty()) {
            double totalActual = obtenerValorActualTotal(usuarioId);
            if (totalActual > 0) {
                valorPorFecha.put(LocalDate.now(), totalActual);
            }
        }

        return valorPorFecha.entrySet().stream()
                .map(entry -> new ValorHistoricoInversion(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<CuentaInversion> obtenerCuentasPorUsuario(Integer usuarioId) throws SQLException {
        return cuentaDAO.obtenerPorUsuario(usuarioId);
    }
}
