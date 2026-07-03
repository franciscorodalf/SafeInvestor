package es.franciscorodalf.safeinvestor.movimientos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long>,
                                              JpaSpecificationExecutor<Movimiento> {

    Optional<Movimiento> findByIdAndUsuario(Long id, Usuario usuario);
}
