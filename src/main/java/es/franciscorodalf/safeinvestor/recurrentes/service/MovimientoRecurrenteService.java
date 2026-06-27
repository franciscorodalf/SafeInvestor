package es.franciscorodalf.safeinvestor.recurrentes.service;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import es.franciscorodalf.safeinvestor.movimientos.domain.Categoria;
import es.franciscorodalf.safeinvestor.movimientos.domain.CategoriaRepository;
import es.franciscorodalf.safeinvestor.movimientos.domain.TipoMovimiento;
import es.franciscorodalf.safeinvestor.recurrentes.domain.Frecuencia;
import es.franciscorodalf.safeinvestor.recurrentes.domain.MovimientoRecurrente;
import es.franciscorodalf.safeinvestor.recurrentes.domain.MovimientoRecurrenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class MovimientoRecurrenteService {

    private final MovimientoRecurrenteRepository repo;
    private final CategoriaRepository categorias;

    public MovimientoRecurrenteService(MovimientoRecurrenteRepository repo,
                                       CategoriaRepository categorias) {
        this.repo = repo;
        this.categorias = categorias;
    }

    public List<MovimientoRecurrente> findAll(Usuario usuario) {
        return repo.findByUsuarioOrderByProximaEjecucionAsc(usuario);
    }

    @Transactional
    public MovimientoRecurrente create(Usuario usuario, Long categoriaId, TipoMovimiento tipo,
                                       BigDecimal importe, String descripcion,
                                       Frecuencia frecuencia, LocalDate proximaEjecucion) {
        Categoria categoria = resolveCategoria(usuario, categoriaId);
        return repo.save(new MovimientoRecurrente(usuario, categoria, tipo, importe,
                descripcion, frecuencia, proximaEjecucion));
    }

    @Transactional
    public void toggleActivo(Usuario usuario, Long id) {
        repo.findByIdAndUsuario(id, usuario).ifPresent(r -> r.setActivo(!r.isActivo()));
    }

    @Transactional
    public void delete(Usuario usuario, Long id) {
        repo.findByIdAndUsuario(id, usuario).ifPresent(repo::delete);
    }

    private Categoria resolveCategoria(Usuario usuario, Long categoriaId) {
        if (categoriaId == null) return null;
        return categorias.findByIdAndUsuario(categoriaId, usuario)
                .orElseThrow(() -> new IllegalArgumentException("Categoría inválida"));
    }
}
