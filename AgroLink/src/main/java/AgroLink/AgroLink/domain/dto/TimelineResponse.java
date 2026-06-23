package AgroLink.AgroLink.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para el Timeline del historial de cambios de estado de un Pedido.
 *
 * Diseñado en snake_case para que el equipo de Frontend pueda ejecutar su .map()
 * directamente sobre la respuesta JSON sin transformaciones adicionales.
 *
 * Consumido por: GET /pedidos/{idPedido}/timeline  (endpoint a implementar).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineResponse {

    /**
     * Marca de tiempo exacta en que ocurrió la transición de estado.
     * Formato ISO-8601: "2025-06-22T14:30:00" para facilitar el parsing en JS (new Date()).
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fecha_registro;

    /**
     * Descripción legible del estado anterior al cambio.
     * Será NULL (y el Frontend lo mostrará como "Inicio") si es la primera asignación de estado.
     * Ejemplo: "Pendiente de pago"
     */
    private String estado_anterior;

    /**
     * Descripción legible del estado al que transicionó el pedido.
     * Ejemplo: "En preparación"
     */
    private String estado_nuevo;

    /**
     * Texto descriptivo del proceso o razón del cambio.
     * Ejemplo: "Pago confirmado por pasarela de cobro"
     */
    private String etapa;

    /**
     * Identificación formateada del usuario que gatilló la acción.
     * Formato: "Nombres Apellido Paterno (ROL)"
     * Ejemplo: "Carlos Mendoza (AGRICULTOR)" o "Sistema (ADMIN)"
     */
    private String usuario_accion;
}
