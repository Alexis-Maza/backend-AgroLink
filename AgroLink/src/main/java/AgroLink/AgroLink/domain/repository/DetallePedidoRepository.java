package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Cultivo;
import AgroLink.AgroLink.persistance.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad DetallePedido.
 * Usa exclusivamente Query Methods por convención de nombres (sin @Query).
 */
@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    /**
     * Obtiene todas las líneas de pedido asociadas a un conjunto de cultivos.
     * Usado para calcular las ventas del agricultor (sus cultivos → sus DetallePedido).
     *
     * Spring traduce esto a:
     * WHERE dp.id_cultivo IN (:cultivos)
     *
     * @param cultivos Lista de cultivos del agricultor autenticado.
     * @return Lista de DetallePedido vinculados a esos cultivos.
     */
    List<DetallePedido> findByCultivoIn(List<Cultivo> cultivos);
}
