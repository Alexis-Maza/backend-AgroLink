package AgroLink.AgroLink.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import AgroLink.AgroLink.persistance.entity.UnidadMedidaProducto;

import java.util.Optional;

public interface UnidadMedidaProductoRepository extends JpaRepository<UnidadMedidaProducto, Long> {

    Optional<UnidadMedidaProducto> findByNombreUnidadMedidaProductoIgnoreCase(String nombre);
}