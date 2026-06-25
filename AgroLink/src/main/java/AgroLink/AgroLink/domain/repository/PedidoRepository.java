package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Pedido;
import AgroLink.AgroLink.persistance.entity.Comprador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByComprador(Comprador comprador);

    // Métodos para reportes
    List<Pedido> findByCompradorAndFechaCreacionBetween(
            Comprador comprador, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}   