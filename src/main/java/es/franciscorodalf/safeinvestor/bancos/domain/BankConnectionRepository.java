package es.franciscorodalf.safeinvestor.bancos.domain;

import es.franciscorodalf.safeinvestor.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankConnectionRepository extends JpaRepository<BankConnection, Long> {

    List<BankConnection> findByUsuarioOrderByCreatedAtDesc(Usuario usuario);

    Optional<BankConnection> findByIdAndUsuario(Long id, Usuario usuario);

    Optional<BankConnection> findByRequisitionId(String requisitionId);

    List<BankConnection> findByStatus(BankConnection.Status status);
}
