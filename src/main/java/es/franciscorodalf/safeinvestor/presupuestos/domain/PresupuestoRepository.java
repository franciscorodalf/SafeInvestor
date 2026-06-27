package es.franciscorodalf.safeinvestor.presupuestos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    List<Presupuesto> findByUsuarioAndAnioAndMesOrderByCategoria_Nombre(Usuario usuario, int anio, int mes);

    Optional<Presupuesto> findByUsuarioAndCategoria_IdAndAnioAndMes(
            Usuario usuario, Long categoriaId, int anio, int mes);

    Optional<Presupuesto> findByIdAndUsuario(Long id, Usuario usuario);
}
