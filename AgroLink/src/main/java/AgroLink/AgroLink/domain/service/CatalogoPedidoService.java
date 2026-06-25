package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.PedidoRequestDTO;
import AgroLink.AgroLink.domain.dto.PedidoResponseDTO;
import AgroLink.AgroLink.domain.repository.*;
import AgroLink.AgroLink.persistance.entity.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

        // 1. Obtener comprador desde el token
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Comprador comprador = compradorRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Perfil de comprador no encontrado"));

        // 2. Obtener estado "Pendiente"
        Estado_Pedido estadoPendiente = estadoPedidoRepository
                .findByDescripcionEstadoPedido("Pendiente")
                .orElseGet(() -> {
                    Estado_Pedido nuevo = new Estado_Pedido();
                    nuevo.setDescripcionEstadoPedido("Pendiente");
                    return estadoPedidoRepository.save(nuevo);
                });

        // 3. Obtener unidad de medida "Kg" (id=1)
        UnidadMedidaProducto unidadKg = unidadMedidaProductoRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Unidad de medida 'Kg' no encontrada"));

        // 4. Crear cabecera del pedido
        Pedido pedido = new Pedido();
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setComprador(comprador);
        pedido.setEstadoPedido(estadoPendiente);

        List<DetallePedido> detalles = new ArrayList<>();

        // 5. Recorrer items del carrito
        for (PedidoRequestDTO.ItemCarritoDTO item : request.getItems()) {

            Cultivo cultivo = cultivoRepository.findById(item.getCultivoId())
                    .orElseThrow(() -> new RuntimeException(
                        "Cultivo con ID " + item.getCultivoId() + " no encontrado"));

            BigDecimal cantidadSolicitada = BigDecimal.valueOf(item.getCantidad());

            // Validar stock disponible
            if (cultivo.getCantidadDisponible().compareTo(cantidadSolicitada) < 0) {
                throw new RuntimeException(
                    "Stock insuficiente para: "
                    + cultivo.getProductoVariedad().getNombreProductosVariedad()
                    + ". Disponible: " + cultivo.getCantidadDisponible()
                    + " " + cultivo.getUnidad()
                );
            }

            // Descontar del stock disponible
            cultivo.setCantidadDisponible(
                cultivo.getCantidadDisponible().subtract(cantidadSolicitada)
            );
            cultivoRepository.save(cultivo);

            // Evaluar si stock bajo a minimo y enviar alerta inmediata
            if (esStockMinimo(cultivo)) {
                try {
                    String nombreAgricultor = cultivo.getAgricultor().getUsuario().getNombres();
                    String emailAgricultor = cultivo.getAgricultor().getUsuario().getEmail();
                    String lote = cultivo.getLote() != null ? cultivo.getLote() : "Sin lote";
                    String producto = cultivo.getProductoVariedad().getNombreProductosVariedad();
                    String unidad = cultivo.getUnidad() != null ? cultivo.getUnidad() : "";

                    emailService.sendAlertaStockMinimo(
                            emailAgricultor,
                            nombreAgricultor != null ? nombreAgricultor : "Agricultor",
                            lote,
                            producto,
                            cultivo.getCantidadDisponible().toPlainString(),
                            cultivo.getMinimoVenta().toPlainString(),
                            unidad
                    );
                    System.out.println("[CatalogoPedidoService] Alerta stock minimo enviada a " + emailAgricultor
                            + " para cultivo ID: " + cultivo.getId());
                } catch (MessagingException ex) {
                    System.err.println("[CatalogoPedidoService] Error enviando alerta stock: " + ex.getMessage());
                }
            }

            // Crear detalle
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setCultivo(cultivo);
            detalle.setCantidadSolicitada(cantidadSolicitada);
            detalle.setPrecioPactado(BigDecimal.valueOf(item.getPrecioPactado()));
            detalle.setCantidadEntrega(BigDecimal.ZERO);
            detalle.setDireccion(item.getDireccionEntrega());
            detalle.setUnidadMedidaProducto(unidadKg);

            detalles.add(detalle);
        }

        pedido.setDetalles(detalles);
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        return mapearPedidoAResponse(pedidoGuardado);
    }

    // ── Mapper ────────────────────────────────────────────────────────────

    private PedidoResponseDTO mapearPedidoAResponse(Pedido pedido) {
        List<PedidoResponseDTO.DetalleResponseDTO> detallesDTO = pedido.getDetalles()
                .stream()
                .map(d -> new PedidoResponseDTO.DetalleResponseDTO(
                        d.getCultivo().getId(),
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