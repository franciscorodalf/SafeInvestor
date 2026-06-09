package es.franciscorodalf.safeinvestor.objetivos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ObjetivoRepository extends JpaRepository<Objetivo, Long> {

    Optional<Objetivo> findByIdAndUsuario(Long id, Usuario usuario);

    @Query("""
        SELECT o FROM Objetivo o
        WHERE o.usuario = :usuario
        ORDER BY
          CASE WHEN o.completadoAt IS NULL THEN 0 ELSE 1 END,
          o.fechaLimite ASC NULLS LAST,
          o.id DESC
        """)
    List<Objetivo> findAllOrdered(@Param("usuario") Usuario usuario);
}
