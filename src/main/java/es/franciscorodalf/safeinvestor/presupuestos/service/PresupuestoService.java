package es.franciscorodalf.safeinvestor.presupuestos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import es.franciscorodalf.safeinvestor.movimientos.domain.CategoriaRepository;
import es.franciscorodalf.safeinvestor.presupuestos.domain.Presupuesto;
import es.franciscorodalf.safeinvestor.presupuestos.domain.PresupuestoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CRUD de presupuestos mensuales por categoría + cálculo de consumo real
 * (suma de movimientos tipo GASTO en esa categoría/mes) para alimentar
 * la vista con barras de progreso y avisos de exceso.
 */
@Service
public class PresupuestoService {

    private final PresupuestoRepository presupuestos;
    private final CategoriaRepository categorias;

    @PersistenceContext
    private EntityManager em;

    public PresupuestoService(PresupuestoRepository presupuestos, CategoriaRepository categorias) {
        this.presupuestos = presupuestos;
        this.categorias = categorias;
    }

    public List<Presupuesto> findAll(Usuario usuario, int anio, int mes) {
        return presupuestos.findByUsuarioAndAnioAndMesOrderByCategoria_Nombre(usuario, anio, mes);
    }

    /**
     * Crea o actualiza el presupuesto de (categoría, año, mes). Si ya existe,
     * sustituye el límite — esto permite que el formulario inline funcione
     * con un único endpoint.
     */
    @Transactional
    public Presupuesto upsert(Usuario usuario, Long categoriaId, int anio, int mes, BigDecimal limite) {
        if (limite == null || limite.signum() <= 0) {
            throw new IllegalArgumentException("El límite debe ser positivo");
        }
        Categoria categoria = categorias.findByIdAndUsuario(categoriaId, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Categoría inválida: " + categoriaId));
        return presupuestos.findByUsuarioAndCategoria_IdAndAnioAndMes(usuario, categoriaId, anio, mes)
                .map(existing -> {
                    existing.setLimite(limite);
                    return existing;
                })
                .orElseGet(() -> presupuestos.save(new Presupuesto(usuario, categoria, anio, mes, limite)));
    }

    @Transactional
    public void delete(Usuario usuario, Long id) {
        presupuestos.findByIdAndUsuario(id, usuario).ifPresent(presupuestos::delete);
    }

    /** Consumo real (suma de gastos) por categoría en el periodo dado. Mapa categoriaId → total gastado. */
    public Map<Long, BigDecimal> consumoPorCategoria(Usuario usuario, int anio, int mes) {
        YearMonth ym = YearMonth.of(anio, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
                SELECT m.categoria_id, COALESCE(SUM(m.importe), 0)
                FROM movimientos m
                WHERE m.usuario_id   = :usuarioId
                  AND m.tipo         = 'GASTO'
                  AND m.categoria_id IS NOT NULL
                  AND m.fecha >= :inicio AND m.fecha <= :fin
                GROUP BY m.categoria_id
                """)
                .setParameter("usuarioId", usuario.getId())
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();

        Map<Long, BigDecimal> out = new HashMap<>();
        for (Object[] r : rows) {
            Long catId = ((Number) r[0]).longValue();
            BigDecimal total = (BigDecimal) r[1];
            out.put(catId, total);
        }
        return out;
    }

    /**
     * Compone una vista resumen: cada presupuesto con su consumo, porcentaje y estado.
     * Útil para la plantilla, donde no queremos meter lógica.
     */
    public List<PresupuestoVista> vistaDelMes(Usuario usuario, int anio, int mes) {
        var preset = findAll(usuario, anio, mes);
        var consumos = consumoPorCategoria(usuario, anio, mes);
        return preset.stream()
                .map(p -> {
                    BigDecimal gastado = consumos.getOrDefault(p.getCategoria().getId(), BigDecimal.ZERO);
                    int pct = p.getLimite().signum() == 0
                            ? 0
                            : gastado.multiply(BigDecimal.valueOf(100))
                                     .divide(p.getLimite(), 0, java.math.RoundingMode.HALF_UP)
                                     .intValue();
                    Estado estado;
                    if (pct >= 100)      estado = Estado.EXCEDIDO;
                    else if (pct >= 80)  estado = Estado.ALERTA;
                    else                  estado = Estado.OK;
                    return new PresupuestoVista(p, gastado, Math.min(pct, 100), estado);
                })
                .collect(Collectors.toList());
    }

    public enum Estado { OK, ALERTA, EXCEDIDO }

    public record PresupuestoVista(Presupuesto presupuesto, BigDecimal gastado, int porcentaje, Estado estado) {
        public Long getId()                { return presupuesto.getId(); }
        public Categoria getCategoria()    { return presupuesto.getCategoria(); }
        public BigDecimal getLimite()      { return presupuesto.getLimite(); }
        public BigDecimal getGastado()     { return gastado; }
        public int getPorcentaje()         { return porcentaje; }
        public Estado getEstado()          { return estado; }
    }
}
