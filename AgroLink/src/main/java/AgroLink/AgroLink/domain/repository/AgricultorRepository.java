package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AgricultorRepository extends JpaRepository<Agricultor, Long> {
    Optional<Agricultor> findByUsuario(Usuario usuario);
    Optional<Agricultor> findByUsuarioEmail(String email);
}