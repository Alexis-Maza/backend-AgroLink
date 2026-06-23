package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.PedidoRequestDTO;
import AgroLink.AgroLink.domain.dto.TimelineResponse;
import AgroLink.AgroLink.domain.repository.CompradorRepository;
import AgroLink.AgroLink.domain.repository.CultivoRepository;
import AgroLink.AgroLink.domain.repository.EstadoPedidoRepository;
import AgroLink.AgroLink.domain.repository.HistorialEstadoPedidoRepository;
import AgroLink.AgroLink.domain.repository.PedidoRepository;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import AgroLink.AgroLink.persistance.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CompradorRepository compradorRepository;
    private final CultivoRepository cultivoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final HistorialEstadoPedidoRepository historialEstadoPedidoRepository;
    private final UsuarioRepository usuarioRepository;

    /** Broker STOMP: envía mensajes a los topics suscritos en tiempo real. */
    private final SimpMessagingTemplate messagingTemplate;

    // ── Método existente: crear pedido ───────────────────────────────────────

    @Transactional
    public Pedido crearPedidoMasivo(PedidoRequestDTO request, String emailComprador) {

        // 1. Obtener el comprador autenticado por email
        Comprador comprador = compradorRepository.findByUsuarioEmail(emailComprador)
                .orElseThrow(() -> new RuntimeException("Comprador no encontrado para el email: " + emailComprador));

        // 2. Obtener el estado "Pendiente"
        Estado_Pedido estadoPendiente = estadoPedidoRepository
                .findByDescripcionEstadoPedidoIgnoreCase("Pendiente")
                .orElseThrow(() -> new RuntimeException("Estado 'Pendiente' no encontrado en la base de datos"));

        // 3. Crear el pedido cabecera
        Pedido pedido = new Pedido();
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setComprador(comprador);
        pedido.setEstadoPedido(estadoPendiente);
        pedido.setDetalles(new ArrayList<>());

        // 4. Crear los detalles por cada item del carrito
        for (PedidoRequestDTO.ItemCarritoDTO item : request.getItems()) {

            Cultivo cultivo = cultivoRepository.findById(item.getCultivoId())
                    .orElseThrow(() -> new RuntimeException("Cultivo no encontrado con id: " + item.getCultivoId()));

            DetallePedido detalle = new DetallePedido();
            detalle.setCantidadSolicitada(BigDecimal.valueOf(item.getCantidad()));
            detalle.setPrecioPactado(BigDecimal.valueOf(item.getPrecioPactado()));
            detalle.setCantidadEntrega(BigDecimal.ZERO);
            detalle.setDireccion(item.getDireccionEntrega());
            detalle.setPedido(pedido);
            detalle.setCultivo(cultivo);

            pedido.getDetalles().add(detalle);
        }

        // 5. Guardar y retornar
        return pedidoRepository.save(pedido);
    }

    // ── Nuevo método: obtener timeline del historial de estados ──────────────

    /**
     * Devuelve la línea de tiempo completa de cambios de estado de un Pedido,
     * ordenada cronológicamente de más antiguo a más reciente, lista para que
     * el Frontend dibuje el Timeline sin transformaciones adicionales.
     *
     * @param idPedido identificador del Pedido cuyo historial se consulta.
     * @return lista de {@link TimelineResponse} ordenada por fechaRegistro ASC.
     * @throws RuntimeException si no existe un Pedido con el id indicado.
     */
    @Transactional(readOnly = true)
    public List<TimelineResponse> obtenerTimelinePedido(Long idPedido) {

        // ── 1. Verificar existencia del Pedido ───────────────────────────────
        //      Usa el método nativo findById sin @Query.
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado con id: " + idPedido));

        // ── 2. Recuperar historial ordenado cronológicamente ─────────────────
        //      Query Method por convención: sin @Query ni SQL manual.
        List<HistorialEstadoPedido> historial =
                historialEstadoPedidoRepository.findByPedidoOrderByFechaRegistroAsc(pedido);

        // ── 3. Convertir entidades → DTOs usando Java Streams ────────────────
        return historial.stream()
                .map(this::mapearATimelineResponse)
                .toList();
    }

    // ── Método auxiliar de mapeo ─────────────────────────────────────────────

    /**
     * Transforma una entidad {@link HistorialEstadoPedido} en el DTO {@link TimelineResponse}.
     * <ul>
     *   <li><b>estado_anterior</b>: null si es el primer estado (creación del pedido).</li>
     *   <li><b>usuario_accion</b>: formateado como "Nombres Apellido (ROL)".</li>
     * </ul>
     */
    private TimelineResponse mapearATimelineResponse(HistorialEstadoPedido h) {

        // estado_anterior puede ser null cuando es el primer estado registrado
        String estadoAnteriorDesc = (h.getEstadoAnterior() != null)
                ? h.getEstadoAnterior().getDescripcionEstadoPedido()
                : null;

        // usuario_accion formateado: "Carlos Mendoza (AGRICULTOR)"
        Usuario actor = h.getUsuarioAccion();
        String usuarioFormateado = actor.getNombres()
                + " " + actor.getApellidoPaterno()
                + " (" + actor.getRol().name() + ")";

        return TimelineResponse.builder()
                .fecha_registro(h.getFechaRegistro())
                .estado_anterior(estadoAnteriorDesc)
                .estado_nuevo(h.getEstadoNuevo().getDescripcionEstadoPedido())
                .etapa(h.getEtapa())
                .usuario_accion(usuarioFormateado)
                .build();
    }

    // ── Nuevo método: actualizar estado + auditoría + notificación WS ────────

    /**
     * Actualiza el estado de un Pedido, registra la auditoría en el historial
     * y notifica en tiempo real a todos los clientes suscritos al topic del pedido.
     *
     * <p>Pasos ejecutados dentro de la misma transacción:</p>
     * <ol>
     *   <li><b>A</b> – Busca el Pedido por ID (findById nativo).</li>
     *   <li><b>B</b> – Busca el nuevo Estado_Pedido por nombre (Query Method).</li>
     *   <li><b>C</b> – Captura el estado actual como «estadoAnterior».</li>
     *   <li><b>D</b> – Actualiza el estado del pedido y persiste con save().</li>
     *   <li><b>E</b> – Crea y guarda el registro de auditoría en historial_estado_pedido.</li>
     *   <li><b>F</b> – Emite el nuevo TimelineResponse vía SimpMessagingTemplate
     *               al topic {@code /topic/pedido/{idPedido}}.</li>
     * </ol>
     *
     * @param idPedido         ID del pedido a actualizar.
     * @param nuevoEstadoNombre Nombre del estado destino (ej. "En preparación").
     * @param emailUsuario     Email del usuario autenticado que realiza la acción.
     * @return {@link TimelineResponse} representando la transición recién registrada.
     */
    @Transactional
    public TimelineResponse actualizarEstadoPedido(Long idPedido,
                                                   String nuevoEstadoNombre,
                                                   String emailUsuario) {

        // ── Paso A: Verificar existencia del Pedido ──────────────────────────
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RuntimeException(
                        "Pedido no encontrado con id: " + idPedido));

        // ── Paso B: Buscar el nuevo estado por nombre (sin @Query) ───────────
        Estado_Pedido estadoNuevo = estadoPedidoRepository
                .findByDescripcionEstadoPedidoIgnoreCase(nuevoEstadoNombre)
                .orElseThrow(() -> new RuntimeException(
                        "Estado de pedido no encontrado: '" + nuevoEstadoNombre + "'"));

        // ── Paso C: Guardar el estado actual como referencia de auditoría ────
        Estado_Pedido estadoAnterior = pedido.getEstadoPedido();

        // ── Paso D: Actualizar el estado del pedido y persistir ──────────────
        pedido.setEstadoPedido(estadoNuevo);
        pedidoRepository.save(pedido);

        // ── Paso E: Registrar auditoría en historial_estado_pedido ───────────
        //           Resolvemos el usuario sin @Query, usando findByEmail existente.
        Usuario actor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado para el email: " + emailUsuario));

        HistorialEstadoPedido registro = new HistorialEstadoPedido();
        registro.setPedido(pedido);
        registro.setEstadoAnterior(estadoAnterior);   // null si es el primer cambio
        registro.setEstadoNuevo(estadoNuevo);
        registro.setEtapa("Cambio de estado: "
                + (estadoAnterior != null ? estadoAnterior.getDescripcionEstadoPedido() : "Inicio")
                + " → " + estadoNuevo.getDescripcionEstadoPedido());
        registro.setUsuarioAccion(actor);
        // fechaRegistro se asigna automáticamente por @PrePersist en la entidad
        historialEstadoPedidoRepository.save(registro);

        // ── Paso F: Construir DTO de respuesta y emitir por WebSocket ────────
        String usuarioFormateado = actor.getNombres()
                + " " + actor.getApellidoPaterno()
                + " (" + actor.getRol().name() + ")";

        TimelineResponse notificacion = TimelineResponse.builder()
                .fecha_registro(registro.getFechaRegistro() != null
                        ? registro.getFechaRegistro()
                        : LocalDateTime.now())
                .estado_anterior(estadoAnterior != null
                        ? estadoAnterior.getDescripcionEstadoPedido()
                        : null)
                .estado_nuevo(estadoNuevo.getDescripcionEstadoPedido())
                .etapa(registro.getEtapa())
                .usuario_accion(usuarioFormateado)
                .build();

        // Notifica a todos los clientes suscritos al topic de este pedido
        // El Frontend escucha: client.subscribe('/topic/pedido/42', callback)
        messagingTemplate.convertAndSend(
                "/topic/pedido/" + idPedido,
                notificacion);

        return notificacion;
    }
}