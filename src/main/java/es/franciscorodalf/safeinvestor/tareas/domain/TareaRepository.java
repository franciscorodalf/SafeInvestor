package es.franciscorodalf.safeinvestor.tareas.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TareaRepository extends JpaRepository<Tarea, Long> {

    Optional<Tarea> findByIdAndUsuario(Long id, Usuario usuario);

    @Query("""
        SELECT t FROM Tarea t
        WHERE t.usuario = :usuario
        ORDER BY
          CASE WHEN t.completadaAt IS NULL THEN 0 ELSE 1 END,
          t.fechaVencimiento ASC NULLS LAST,
          t.id DESC
        """)
    List<Tarea> findAllOrdered(@Param("usuario") Usuario usuario);

    /** Devuelve todas las tareas no completadas con fecha de vencimiento anterior a hoy. */
    @Query("""
        SELECT t FROM Tarea t
        WHERE t.completadaAt IS NULL
          AND t.fechaVencimiento IS NOT NULL
          AND t.fechaVencimiento < :hoy
        ORDER BY t.usuario.id, t.fechaVencimiento ASC
        """)
    List<Tarea> findAllVencidas(@Param("hoy") java.time.LocalDate hoy);
}
