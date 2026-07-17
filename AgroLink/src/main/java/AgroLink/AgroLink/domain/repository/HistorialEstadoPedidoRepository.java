package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.HistorialEstadoPedido;
import AgroLink.AgroLink.persistance.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoPedidoRepository extends JpaRepository<HistorialEstadoPedido, Long> {

    List<HistorialEstadoPedido> findByPedidoOrderByFechaRegistroAsc(Pedido pedido);
    List<HistorialEstadoPedido> findByPedido_Comprador_IdOrderByFechaRegistroDesc(Long compradorId);
    List<HistorialEstadoPedido> findDistinctByPedido_Detalles_Cultivo_Agricultor_IdAndEstadoAnteriorIsNullOrderByFechaRegistroDesc(Long agricultorId);

}
