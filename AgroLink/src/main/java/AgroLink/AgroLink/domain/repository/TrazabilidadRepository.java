package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Cultivo;
import AgroLink.AgroLink.persistance.entity.DetallePedido;
import AgroLink.AgroLink.persistance.entity.Historial_Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * RF-TRAZABILIDAD: Repositorio dedicado a las consultas de trazabilidad.
 *
 * Estrategia de performance:
 *  - Se usan consultas JPQL con JOIN FETCH para cargar en una sola query
 *    todas las asociaciones LAZY necesarias, eliminando el problema N+1.
 *  - Cada método cubre exactamente un "tramo" de la cadena de trazabilidad,
 *    permitiendo cargarlos de forma paralela/secuencial sin redundancia.
 */
@Repository
public interface TrazabilidadRepository extends JpaRepository<Cultivo, Long> {

    /**
     * Carga el cultivo con agricultor → usuario y productoVariedad → producto
     * en un único JOIN FETCH.
     *
     * Evita N+1 sobre las relaciones LAZY de Cultivo, Agricultor y Usuario.
     *
     * SQL generado (simplificado):
     *   SELECT c.*, a.*, u.*, pv.*, p.*
     *   FROM cultivo c
     *   JOIN agricultores a ON c.id_agricultor = a.id
     *   JOIN usuarios u ON a.id_usuario = u.id
     *   JOIN productos_variedad pv ON c.id_producto_variedad = pv.id
     *   JOIN producto p ON pv.id_producto = p.id
     *   JOIN estado_cultivo ec ON c.id_estado_cultivo = ec.id
     *   WHERE c.id = :id
     */
    @Query("""
        SELECT c FROM Cultivo c
        JOIN FETCH c.agricultor a
        JOIN FETCH a.usuario u
        JOIN FETCH c.productoVariedad pv
        JOIN FETCH pv.producto p
        JOIN FETCH c.estadoCultivo ec
        WHERE c.id = :id
    """)
    Optional<Cultivo> findByIdWithAgricultorAndProducto(@Param("id") Long id);

    /**
     * Carga todo el historial de etapas del cultivo en orden cronológico,
     * haciendo JOIN FETCH sobre estadoCultivo y etapaProductoVariedad (puede ser null).
     *
     * LEFT JOIN FETCH en etapaProductoVariedad porque es opcional (nullable FK).
     *
     * SQL generado (simplificado):
     *   SELECT hc.*, ec.*, epv.*
     *   FROM historial_cultivo hc
     *   JOIN estado_cultivo ec ON hc.id_estado_cultivo = ec.id
     *   LEFT JOIN etapa_productos_variedad epv ON hc.id_etapa_productos_variedad = epv.id
     *   WHERE hc.id_cultivo = :idCultivo
     *   ORDER BY hc.fecha_inicio ASC
     */
    @Query("""
        SELECT hc FROM Historial_Cultivo hc
        JOIN FETCH hc.estadoCultivo ec
        LEFT JOIN FETCH hc.etapaProductoVariedad epv
        WHERE hc.cultivo.id = :idCultivo
        ORDER BY hc.fechaInicio ASC
    """)
    List<Historial_Cultivo> findHistorialByCultivoId(@Param("idCultivo") Long idCultivo);

    /**
     * Carga todos los DetallePedido de un cultivo con sus pedidos,
     * compradores, usuarios del comprador, estado del pedido y unidad de medida,
     * todo en una sola query con múltiples JOIN FETCH.
     *
     * Evita N+1 sobre: Pedido, Comprador, Usuario, Estado_Pedido, UnidadMedidaProducto.
     *
     * SQL generado (simplificado):
     *   SELECT dp.*, ped.*, comp.*, ucomp.*, ep.*, ump.*
     *   FROM detalle_pedido dp
     *   JOIN pedido ped ON dp.id_pedido = ped.id_pedido
     *   JOIN compradores comp ON ped.id_comprador = comp.id
     *   JOIN usuarios ucomp ON comp.id_usuario = ucomp.id
     *   JOIN estado_pedido ep ON ped.id_estado_pedido = ep.id
     *   JOIN unidad_medida_producto ump ON dp.id_unidad_medida_producto = ump.id
     *   WHERE dp.id_cultivo = :idCultivo
     *   ORDER BY ped.fecha_creacion DESC
     */
    @Query("""
        SELECT dp FROM DetallePedido dp
        JOIN FETCH dp.pedido ped
        JOIN FETCH ped.comprador comp
        JOIN FETCH comp.usuario ucomp
        JOIN FETCH ped.estadoPedido ep
        JOIN FETCH dp.unidadMedidaProducto ump
        WHERE dp.cultivo.id = :idCultivo
        ORDER BY ped.fechaCreacion DESC
    """)
    List<DetallePedido> findDetallesPedidoByCultivoId(@Param("idCultivo") Long idCultivo);
}
