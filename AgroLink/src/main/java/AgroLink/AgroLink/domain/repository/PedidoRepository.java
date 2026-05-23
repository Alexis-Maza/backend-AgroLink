package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}