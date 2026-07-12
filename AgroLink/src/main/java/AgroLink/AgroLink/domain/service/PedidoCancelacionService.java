package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.TimelineResponse;
import AgroLink.AgroLink.domain.repository.DetallePedidoRepository;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * RF-AF-07: cancela automáticamente los pedidos que se quedan sin stock suficiente
 * (por ejemplo, tras registrar una merma) y notifica al comprador.
 */
@Service
@RequiredArgsConstructor
public class PedidoCancelacionService {

    /** Estados de pedido en los que el producto sigue en la finca (aún no despachado) y puede verse afectado por una merma. */
    public static final List<String> ESTADOS_ACTIVOS = List.of("Pendiente", "En preparación");
    private static final String ESTADO_CANCELADO = "Cancelado";

    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final HistorialEstadoPedidoRepository historialEstadoPedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Evalúa los pedidos activos ligados a un cultivo tras un cambio de stock (ej. una merma)
     * y cancela los que ya no quepan en el stock físico restante del cultivo.
     *
     * <p>El stock total restante se calcula como {@code cantidadDisponible + reservado en
     * pedidos activos} (lo no vendido más lo ya comprometido pero aún no despachado). Los
     * pedidos se priorizan por antigüedad (el más antiguo tiene preferencia); en cuanto la
     * suma acumulada de cantidades solicitadas supera el stock total restante, ese pedido y
     * los más nuevos que ya no quepan se cancelan.</p>
     *
     * @param cultivo   Cultivo cuyo stock acaba de cambiar.
     * @param motivoBase Texto descriptivo de la causa (ej. "Merma reportada: plaga").
     */
    @Transactional
    public void evaluarCancelacionesPorCultivo(Cultivo cultivo, String motivoBase) {
        List<DetallePedido> detallesActivos = detallePedidoRepository
                .findByCultivoAndPedido_EstadoPedido_DescripcionEstadoPedidoIn(cultivo, ESTADOS_ACTIVOS);

        detallesActivos.sort(Comparator.comparing(d -> d.getPedido().getFechaCreacion()));

        BigDecimal stockTotalRestante = detallesActivos.stream()
                .map(DetallePedido::getCantidadSolicitada)
                .reduce(cultivo.getCantidadDisponible(), BigDecimal::add);

        BigDecimal acumulado = BigDecimal.ZERO;
        Set<Pedido> pedidosACancelar = new LinkedHashSet<>();
        for (DetallePedido detalle : detallesActivos) {
            acumulado = acumulado.add(detalle.getCantidadSolicitada());
            if (acumulado.compareTo(stockTotalRestante) > 0) {
                pedidosACancelar.add(detalle.getPedido());
            }
        }

        for (Pedido pedido : pedidosACancelar) {
            cancelarPedido(pedido, motivoBase);
        }
    }

    private void cancelarPedido(Pedido pedido, String motivo) {
        Estado_Pedido estadoCancelado = estadoPedidoRepository
                .findByDescripcionEstadoPedidoIgnoreCase(ESTADO_CANCELADO)
                .orElseThrow(() -> new RuntimeException(
                        "Estado de pedido 'Cancelado' no encontrado en la base de datos"));

        Usuario usuarioSistema = usuarioRepository
                .findByEmail(DatabaseSeeder.EMAIL_USUARIO_SISTEMA)
                .orElseThrow(() -> new RuntimeException("Usuario sistema no encontrado"));

        Estado_Pedido estadoAnterior = pedido.getEstadoPedido();
        pedido.setEstadoPedido(estadoCancelado);
        pedidoRepository.save(pedido);

        HistorialEstadoPedido registro = new HistorialEstadoPedido();
        registro.setPedido(pedido);
        registro.setEstadoAnterior(estadoAnterior);
        registro.setEstadoNuevo(estadoCancelado);
        registro.setEtapa(motivo);
        registro.setUsuarioAccion(usuarioSistema);
        historialEstadoPedidoRepository.save(registro);

        TimelineResponse notificacion = TimelineResponse.builder()
                .fecha_registro(registro.getFechaRegistro())
                .estado_anterior(estadoAnterior != null ? estadoAnterior.getDescripcionEstadoPedido() : null)
                .estado_nuevo(estadoCancelado.getDescripcionEstadoPedido())
                .etapa(motivo)
                .usuario_accion("Sistema AgroLink")
                .build();

        messagingTemplate.convertAndSend("/topic/pedido/" + pedido.getId(), notificacion);

        Comprador comprador = pedido.getComprador();
        String nombreComprador = comprador.getUsuario().getNombres();
        String producto = pedido.getDetalles().isEmpty() ? "tu pedido"
                : pedido.getDetalles().get(0).getCultivo().getProductoVariedad().getNombreProductosVariedad();

        try {
            emailService.sendCancelacionPedidoPorStock(
                    comprador.getUsuario().getEmail(), nombreComprador, pedido.getId(), producto, motivo);
        } catch (Exception e) {
            System.err.println("Error al enviar email de cancelación: " + e.getMessage());
        }

        try {
            whatsAppService.sendCancelacionPedidoPorStock(
                    comprador.getTelefono(), nombreComprador, pedido.getId(), producto, motivo);
        } catch (Exception e) {
            System.err.println("Error al enviar WhatsApp de cancelación: " + e.getMessage());
        }
    }
}
