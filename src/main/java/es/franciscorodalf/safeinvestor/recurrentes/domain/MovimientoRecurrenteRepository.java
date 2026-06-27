package es.franciscorodalf.safeinvestor.recurrentes.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MovimientoRecurrenteRepository extends JpaRepository<MovimientoRecurrente, Long> {

    List<MovimientoRecurrente> findByUsuarioOrderByProximaEjecucionAsc(Usuario usuario);

    Optional<MovimientoRecurrente> findByIdAndUsuario(Long id, Usuario usuario);

    /** Recurrencias activas cuya próxima ejecución es hoy o anterior. */
    @Query("SELECT r FROM MovimientoRecurrente r " +
           "WHERE r.activo = true AND r.proximaEjecucion <= :hasta")
    List<MovimientoRecurrente> findDueOn(@Param("hasta") LocalDate hasta);
}
