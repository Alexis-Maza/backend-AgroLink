package AgroLink.AgroLink.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de entrada para el endpoint PATCH /api/v1/pedidos/{idPedido}/estado.
 *
 * <p>Ejemplo de body JSON enviado por el Frontend:</p>
 * <pre>
 * {
 *   "nuevo_estado": "En preparación"
 * }
 * </pre>
 *
 * El valor de {@code nuevo_estado} debe coincidir exactamente (case-insensitive)
 * con la columna {@code descripcion_estado_pedido} de la tabla {@code estado_pedido}.
 */
@Data
@NoArgsConstructor
public class ActualizarEstadoRequest {

    /**
     * Nombre del nuevo estado al que debe transicionar el Pedido.
     * Ejemplo: "Pendiente", "En preparación", "Enviado", "Entregado", "Cancelado".
     */
    private String nuevo_estado;
}
