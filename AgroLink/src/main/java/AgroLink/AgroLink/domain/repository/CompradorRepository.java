package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Comprador;
import AgroLink.AgroLink.persistance.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompradorRepository extends JpaRepository<Comprador, Long> {
    Optional<Comprador> findByUsuario(Usuario usuario);
}
