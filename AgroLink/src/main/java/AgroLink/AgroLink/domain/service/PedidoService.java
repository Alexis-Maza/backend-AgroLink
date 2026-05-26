package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.PedidoRequestDTO;
import AgroLink.AgroLink.domain.repository.CompradorRepository;
import AgroLink.AgroLink.domain.repository.CultivoRepository;
import AgroLink.AgroLink.domain.repository.EstadoPedidoRepository;
import AgroLink.AgroLink.domain.repository.PedidoRepository;
import AgroLink.AgroLink.persistance.entity.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CompradorRepository compradorRepository;
    private final CultivoRepository cultivoRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;

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
}