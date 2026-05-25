package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Estado_Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstadoCultivoRepository extends JpaRepository<Estado_Cultivo, Long> {

    // Buscar estado de cultivo por su descripción (Query Method nativo)
    Optional<Estado_Cultivo> findByDescripcionEstadoCultivo(String descripcionEstadoCultivo);
}
