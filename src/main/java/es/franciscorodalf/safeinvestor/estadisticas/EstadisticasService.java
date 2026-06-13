package es.franciscorodalf.safeinvestor.estadisticas;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.MovimientoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class EstadisticasService {

    @PersistenceContext
    private EntityManager em;

    private final MovimientoRepository movimientos;

    public EstadisticasService(MovimientoRepository movimientos) {
        this.movimientos = movimientos;
    }

    /** Gastos del mes actual agrupados por categoría. */
    public List<CategoriaTotal> gastosPorCategoria(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.withDayOfMonth(1);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT COALESCE(c.nombre, 'Sin categoría') AS nombre,
                   COALESCE(c.color, '#9CA3AF') AS color,
                   SUM(m.importe) AS total
            FROM movimientos m
            LEFT JOIN categorias c ON c.id = m.categoria_id
            WHERE m.usuario_id = :usuarioId
              AND m.tipo = 'GASTO'
              AND m.fecha >= :inicio
              AND m.fecha <= :hoy
            GROUP BY c.nombre, c.color
            ORDER BY total DESC
            """)
            .setParameter("usuarioId", usuario.getId())
            .setParameter("inicio", inicio)
            .setParameter("hoy", hoy)
            .getResultList();

        return rows.stream()
            .map(r -> new CategoriaTotal(
                (String) r[0],
                (String) r[1],
                ((Number) r[2]).doubleValue()))
            .toList();
    }

    /** Ingresos vs gastos por mes durante los últimos 6 meses. */
    public List<MesTotales> evolucionMensual(Usuario usuario) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusMonths(5).withDayOfMonth(1);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT TO_CHAR(date_trunc('month', m.fecha), 'YYYY-MM') AS mes,
                   m.tipo,
                   SUM(m.importe) AS total
            FROM movimientos m
            WHERE m.usuario_id = :usuarioId
              AND m.fecha >= :inicio
            GROUP BY mes, m.tipo
            ORDER BY mes
            """)
            .setParameter("usuarioId", usuario.getId())
            .setParameter("inicio", inicio)
            .getResultList();

        // Indexar por mes
        Map<String, MesTotales> porMes = new LinkedHashMap<>();
        LocalDate cursor = inicio;
        for (int i = 0; i < 6; i++) {
            String key = cursor.toString().substring(0, 7); // YYYY-MM
            porMes.put(key, new MesTotales(key, 0.0, 0.0));
            cursor = cursor.plusMonths(1);
        }
        for (Object[] r : rows) {
            String mes = (String) r[0];
            String tipo = (String) r[1];
            double total = ((Number) r[2]).doubleValue();
            MesTotales m = porMes.get(mes);
            if (m == null) continue;
            if ("INGRESO".equals(tipo)) porMes.put(mes, new MesTotales(mes, total, m.gastos()));
            else porMes.put(mes, new MesTotales(mes, m.ingresos(), total));
        }
        return new ArrayList<>(porMes.values());
    }

    public record CategoriaTotal(String nombre, String color, double total) {}
    public record MesTotales(String mes, double ingresos, double gastos) {}
}
