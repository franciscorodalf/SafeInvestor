package es.franciscorodalf.safeinvestor.movimientos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioOrderByNombre(Usuario usuario);
    Optional<Categoria> findByIdAndUsuario(Long id, Usuario usuario);
    boolean existsByUsuarioAndNombreIgnoreCase(Usuario usuario, String nombre);
}
