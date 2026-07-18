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

    /**
     * Obtiene las líneas de pedido de un cultivo cuyo Pedido está en alguno de los estados indicados.
     * Usado para detectar, tras registrar una merma, qué pedidos activos dependen de ese cultivo.
     *
     * Spring traduce esto a:
     * WHERE dp.id_cultivo = :cultivo AND p.id_estado_pedido IN (SELECT ... descripcion IN :estados)
     *
     * @param cultivo Cultivo cuyo stock cambió.
     * @param estados Descripciones de los estados de pedido considerados "activos".
     * @return Lista de DetallePedido de ese cultivo en pedidos activos.
     */
    List<DetallePedido> findByCultivoAndPedido_EstadoPedido_DescripcionEstadoPedidoIn(
            Cultivo cultivo, List<String> estados);
}
