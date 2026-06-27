package es.franciscorodalf.safeinvestor.movimientos.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import es.franciscorodalf.safeinvestor.movimientos.domain.CategoriaRepository;
import es.franciscorodalf.safeinvestor.movimientos.domain.Movimiento;
import es.franciscorodalf.safeinvestor.movimientos.domain.MovimientoRepository;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovimientoService {

    private static final int PAGE_SIZE = 20;

    private final MovimientoRepository movimientos;
    private final CategoriaRepository categorias;

    public MovimientoService(MovimientoRepository movimientos, CategoriaRepository categorias) {
        this.movimientos = movimientos;
        this.categorias = categorias;
    }

    public Page<Movimiento> search(Usuario usuario, Long categoriaId,
                                   LocalDate desde, LocalDate hasta, int page) {
        return search(usuario, categoriaId, desde, hasta, null, page);
    }

    public Page<Movimiento> search(Usuario usuario, Long categoriaId,
                                   LocalDate desde, LocalDate hasta,
                                   String q, int page) {
        Specification<Movimiento> spec = (root, query, cb) -> {
            List<Predicate> preds = new ArrayList<>();
            preds.add(cb.equal(root.get("usuario"), usuario));
            if (categoriaId != null) {
                preds.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            }
            if (desde != null) {
                preds.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            }
            if (hasta != null) {
                preds.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));
            }
            if (q != null && !q.isBlank()) {
                // ILIKE en Postgres → case-insensitive. Buscamos en descripción.
                String needle = "%" + q.trim().toLowerCase() + "%";
                preds.add(cb.like(cb.lower(root.get("descripcion")), needle));
            }
            return cb.and(preds.toArray(Predicate[]::new));
        };
        return movimientos.findAll(spec,
            PageRequest.of(Math.max(0, page), PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "fecha").and(Sort.by(Sort.Direction.DESC, "id"))));
    }

    public Movimiento get(Usuario usuario, Long id) {
        return movimientos.findByIdAndUsuario(id, usuario)
            .orElseThrow(() -> new MovimientoNotFoundException(id));
    }

    @Transactional
    public Movimiento create(Usuario usuario, Long categoriaId, TipoMovimiento tipo,
                             BigDecimal importe, String descripcion, LocalDate fecha) {
        Categoria categoria = resolveCategoria(usuario, categoriaId);
        return movimientos.save(new Movimiento(usuario, categoria, tipo, importe, descripcion, fecha));
    }

    @Transactional
    public Movimiento update(Usuario usuario, Long id, Long categoriaId, TipoMovimiento tipo,
                             BigDecimal importe, String descripcion, LocalDate fecha) {
        Movimiento m = get(usuario, id);
        m.setCategoria(resolveCategoria(usuario, categoriaId));
        m.setTipo(tipo);
        m.setImporte(importe);
        m.setDescripcion(descripcion);
        m.setFecha(fecha);
        return m;
    }

    @Transactional
    public void delete(Usuario usuario, Long id) {
        Movimiento m = get(usuario, id);
        movimientos.delete(m);
    }

    private Categoria resolveCategoria(Usuario usuario, Long categoriaId) {
        if (categoriaId == null) return null;
        return categorias.findByIdAndUsuario(categoriaId, usuario)
            .orElseThrow(() -> new IllegalArgumentException("Categoría inválida: " + categoriaId));
    }

    public static class MovimientoNotFoundException extends RuntimeException {
        public MovimientoNotFoundException(Long id) { super("Movimiento no encontrado: " + id); }
    }
}
