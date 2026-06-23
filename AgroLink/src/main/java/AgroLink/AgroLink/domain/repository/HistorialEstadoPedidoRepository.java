package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.HistorialEstadoPedido;
import AgroLink.AgroLink.persistance.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio JPA para la entidad {@link HistorialEstadoPedido}.
 * Provee acceso a la tabla historial_estado_pedido sin necesidad de escribir SQL/JPQL.
 */
public interface HistorialEstadoPedidoRepository extends JpaRepository<HistorialEstadoPedido, Long> {

    /**
     * Recupera todos los registros de historial de un Pedido específico
     * ordenados de forma cronológica ascendente (del más antiguo al más reciente).
     * El Frontend usará esta lista ordenada para dibujar el Timeline de izquierda a derecha.
     *
     * @param pedido el Pedido cuyo historial se desea consultar.
     * @return lista de {@link HistorialEstadoPedido} ordenada por fechaRegistro ASC.
     */
    List<HistorialEstadoPedido> findByPedidoOrderByFechaRegistroAsc(Pedido pedido);
}
