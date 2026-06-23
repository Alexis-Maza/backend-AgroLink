package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.ActualizarEstadoRequest;
import AgroLink.AgroLink.domain.dto.TimelineResponse;
import AgroLink.AgroLink.domain.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para operaciones relacionadas con Pedidos.
 *
 * <p>Base URL: {@code /api/v1/pedidos}</p>
 *
 * Endpoints expuestos:
 * <ul>
 *   <li>GET   /api/v1/pedidos/{idPedido}/timeline → Timeline de historial de estados.</li>
 *   <li>PATCH /api/v1/pedidos/{idPedido}/estado   → Cambia el estado y notifica por WS.</li>
 * </ul>
 *
 * Protección: todos los endpoints requieren JWT válido con rol AGRICULTOR o COMPRADOR
 * (ver SecurityConfig → "/api/v1/pedidos/**").
 */
@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // ── GET /api/v1/pedidos/{idPedido}/timeline ──────────────────────────────

    /**
     * Devuelve la línea de tiempo completa de cambios de estado de un Pedido,
     * ordenada cronológicamente (más antiguo → más reciente).
     *
     * @param idPedido identificador del Pedido en la ruta URL.
     * @return 200 OK con la lista de {@link TimelineResponse}.
     */
    @GetMapping("/{idPedido}/timeline")
    public ResponseEntity<List<TimelineResponse>> obtenerTimeline(
            @PathVariable Long idPedido) {

        return ResponseEntity.ok(pedidoService.obtenerTimelinePedido(idPedido));
    }

    // ── PATCH /api/v1/pedidos/{idPedido}/estado ──────────────────────────────

    /**
     * Actualiza el estado de un Pedido, registra la auditoría en el historial
     * y notifica en tiempo real a todos los clientes WebSocket suscritos al
     * topic {@code /topic/pedido/{idPedido}}.
     *
     * <p>Body esperado:</p>
     * <pre>
     * {
     *   "nuevo_estado": "En preparación"
     * }
     * </pre>
     *
     * <p>El valor de {@code nuevo_estado} debe coincidir (case-insensitive) con la
     * descripción almacenada en la tabla {@code estado_pedido}.</p>
     *
     * @param idPedido    ID del Pedido a actualizar.
     * @param request     Body con el nombre del nuevo estado.
     * @param userDetails Principal inyectado por Spring Security desde el JWT.
     * @return 200 OK con el {@link TimelineResponse} de la transición recién registrada.
     */
    @PatchMapping("/{idPedido}/estado")
    public ResponseEntity<TimelineResponse> actualizarEstado(
            @PathVariable Long idPedido,
            @RequestBody ActualizarEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TimelineResponse resultado = pedidoService.actualizarEstadoPedido(
                idPedido,
                request.getNuevo_estado(),
                userDetails.getUsername()   // getUsername() devuelve el email (ver Usuario.java)
        );

        return ResponseEntity.ok(resultado);
    }
}
