package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Estado_Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoPedidoRepository extends JpaRepository<Estado_Pedido, Long> {
    Optional<Estado_Pedido> findByDescripcionEstadoPedido(String descripcion);
    Optional<Estado_Pedido> findByDescripcionEstadoPedidoIgnoreCase(String descripcion);
}