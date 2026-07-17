package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.NotificacionDTO;
import AgroLink.AgroLink.domain.dto.PedidoRequestDTO;
import AgroLink.AgroLink.domain.dto.PedidoResponseDTO;
import AgroLink.AgroLink.domain.exception.StockInsuficienteException;
import AgroLink.AgroLink.domain.repository.*;
import AgroLink.AgroLink.persistance.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogoPedidoService {

    private final CultivoRepository cultivoRepository;
    private final PedidoRepository pedidoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CompradorRepository compradorRepository;
    private final UnidadMedidaProductoRepository unidadMedidaProductoRepository;
    private final EmailService emailService;
    private final HistorialEstadoPedidoRepository historialEstadoPedidoRepository; // <-- NUEVO
    private final SimpMessagingTemplate messagingTemplate;
    private final WhatsAppService whatsAppService;

    // ── Catálogo con filtros ──────────────────────────────────────────────

    public List<Cultivo> obtenerCatalogoAvanzado(
            String search, String region, Double precioMax, Long productoId) {
        return cultivoRepository.findAll(
            CultivoSpecification.filtrarCatalogo(search, region, precioMax, productoId)
        );
    }

    // ── Crear Pedido ──────────────────────────────────────────────────────

    @Transactional
    public PedidoResponseDTO crearPedido(String email, PedidoRequestDTO request) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Comprador comprador = compradorRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Perfil de comprador no encontrado"));

        Estado_Pedido estadoPendiente = estadoPedidoRepository
                .findByDescripcionEstadoPedido("Pendiente")
                .orElseGet(() -> {
                    Estado_Pedido nuevo = new Estado_Pedido();
                    nuevo.setDescripcionEstadoPedido("Pendiente");
                    return estadoPedidoRepository.save(nuevo);
                });

        UnidadMedidaProducto unidadKg = unidadMedidaProductoRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Unidad de medida 'Kg' no encontrada"));

        Pedido pedido = new Pedido();
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setComprador(comprador);
        pedido.setEstadoPedido(estadoPendiente);

        List<DetallePedido> detalles = new ArrayList<>();

        for (PedidoRequestDTO.ItemCarritoDTO item : request.getItems()) {

            Cultivo cultivo = cultivoRepository.findById(item.getCultivoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Cultivo con ID " + item.getCultivoId() + " no encontrado"));

            BigDecimal cantidadSolicitada = BigDecimal.valueOf(item.getCantidad());

            // --- CAMBIADO: excepción estructurada en vez de RuntimeException con string ---
            if (cultivo.getCantidadDisponible().compareTo(cantidadSolicitada) < 0) {
                throw new StockInsuficienteException(
                        cultivo.getProductoVariedad().getNombreProductosVariedad(),
                        cultivo.getCantidadDisponible(),
                        cultivo.getUnidad()
                );
            }

            cultivo.setCantidadDisponible(
                    cultivo.getCantidadDisponible().subtract(cantidadSolicitada)
            );
            cultivoRepository.save(cultivo);

            if (esStockMinimo(cultivo)) {
                // ... igual que ya tenías ...
            }

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setCultivo(cultivo);
            detalle.setCantidadSolicitada(cantidadSolicitada);
            detalle.setPrecioPactado(BigDecimal.valueOf(item.getPrecioPactado()));
            detalle.setCantidadEntrega(BigDecimal.ZERO);
            detalle.setDireccion(item.getDireccionEntrega());
            detalle.setUnidadMedidaProducto(unidadKg);
            detalle.setMetodoPago(item.getMetodoPago());
            detalle.setPorcentajeAdelanto(item.getPorcentajeAdelanto());

            detalles.add(detalle);
        }

        pedido.setDetalles(detalles);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // --- NUEVO: primer punto del timeline ---
        HistorialEstadoPedido historialInicial = new HistorialEstadoPedido();
        historialInicial.setPedido(pedidoGuardado);
        historialInicial.setEstadoAnterior(null);
        historialInicial.setEstadoNuevo(estadoPendiente);
        historialInicial.setEtapa("Pedido creado por el comprador");
        historialInicial.setUsuarioAccion(usuario);
        historialEstadoPedidoRepository.save(historialInicial);

        // --- NUEVO: notificar a cada agricultor involucrado ---
        Set<Long> agricultoresNotificados = new HashSet<>();
        for (DetallePedido detalle : pedidoGuardado.getDetalles()) {
            Long idAgricultor = detalle.getCultivo().getAgricultor().getId();
            if (agricultoresNotificados.add(idAgricultor)) {
                NotificacionDTO notif = new NotificacionDTO(
                        "historial-" + historialInicial.getIdHistorial(),
                        "PEDIDO_RECIBIDO",
                        "Recibiste un nuevo pedido #" + pedidoGuardado.getId(),
                        historialInicial.getFechaRegistro(),
                        pedidoGuardado.getId()
                );
                messagingTemplate.convertAndSend("/topic/agricultor/" + idAgricultor, notif);
            }
        }

        // --- NUEVO: confirmación de pedido al comprador (RF-AF-06) ---
        List<String> nombresProductos = pedidoGuardado.getDetalles().stream()
                .map(d -> d.getCultivo().getProductoVariedad().getNombreProductosVariedad()
                        + " (" + d.getCantidadSolicitada() + " " + d.getCultivo().getUnidad() + ")")
                .collect(Collectors.toList());

        BigDecimal totalPedido = pedidoGuardado.getDetalles().stream()
                .map(d -> d.getCantidadSolicitada().multiply(d.getPrecioPactado()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        try {
            emailService.sendConfirmacionPedido(
                    usuario.getEmail(), usuario.getNombres(), pedidoGuardado.getId(), nombresProductos, totalPedido);
        } catch (Exception e) {
            System.err.println("Error al enviar email de confirmación de pedido: " + e.getMessage());
        }

        try {
            whatsAppService.sendConfirmacionPedido(
                    comprador.getTelefono(), usuario.getNombres(), pedidoGuardado.getId(),
                    String.join(", ", nombresProductos), totalPedido);
        } catch (Exception e) {
            System.err.println("Error al enviar WhatsApp de confirmación de pedido: " + e.getMessage());
        }

        return mapearPedidoAResponse(pedidoGuardado);

    }
    // ── Mapper ────────────────────────────────────────────────────────────

    private PedidoResponseDTO mapearPedidoAResponse(Pedido pedido) {
        List<PedidoResponseDTO.DetalleResponseDTO> detallesDTO = pedido.getDetalles()
                .stream()
                .map(d -> new PedidoResponseDTO.DetalleResponseDTO(
                        d.getCultivo().getId(),
                        d.getCultivo().getProductoVariedad().getProducto() != null
                                ? d.getCultivo().getProductoVariedad().getProducto().getNombre()
                                : "—",
                        d.getCultivo().getProductoVariedad().getNombreProductosVariedad(),
                        d.getCantidadSolicitada(),
                        d.getPrecioPactado(),
                        d.getDireccion()
                ))
                .collect(Collectors.toList());

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getFechaCreacion(),
                pedido.getEstadoPedido().getDescripcionEstadoPedido(),
                detallesDTO
        );
    }

    // ── Helpers para alertas ───────────────────────────────────────────────

    private boolean esStockMinimo(Cultivo cultivo) {
        BigDecimal disponible = cultivo.getCantidadDisponible();
        BigDecimal minimo = cultivo.getMinimoVenta();

        return disponible != null
                && minimo != null
                && disponible.compareTo(BigDecimal.ZERO) >= 0
                && disponible.compareTo(minimo) <= 0;
    }
}