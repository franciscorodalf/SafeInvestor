package es.franciscorodalf.safeinvestor.movimientos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    Optional<Movimiento> findByIdAndUsuario(Long id, Usuario usuario);

    @Query("""
        SELECT m FROM Movimiento m
        WHERE m.usuario = :usuario
          AND (:categoriaId IS NULL OR m.categoria.id = :categoriaId)
          AND (:desde IS NULL OR m.fecha >= :desde)
          AND (:hasta IS NULL OR m.fecha <= :hasta)
        ORDER BY m.fecha DESC, m.id DESC
        """)
    Page<Movimiento> search(@Param("usuario") Usuario usuario,
                            @Param("categoriaId") Long categoriaId,
                            @Param("desde") LocalDate desde,
                            @Param("hasta") LocalDate hasta,
                            Pageable pageable);
}
