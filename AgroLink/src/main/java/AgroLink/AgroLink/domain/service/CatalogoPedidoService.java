package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.PedidoRequestDTO;
import AgroLink.AgroLink.domain.repository.CultivoRepository;
import AgroLink.AgroLink.domain.repository.PedidoRepository;
import AgroLink.AgroLink.persistance.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CatalogoPedidoService {

    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    // 1. LÓGICA DE FILTROS DEL CATÁLOGO
    public List<Cultivo> obtenerCatalogo(String region, Long productoId) {
        String estadoBuscado = "Cosechado";
        Double stockMinimo = 0.0;

        if ((region == null || region.trim().isEmpty()) && productoId == null) {
            return cultivoRepository
                    .findByEstadoCultivo_DescripcionEstadoCultivoAndAreaSembradaGreaterThan(estadoBuscado, stockMinimo);
        } else if (region != null && !region.trim().isEmpty() && productoId == null) {
            return cultivoRepository
                    .findByAgricultor_UbicacionContainingIgnoreCaseAndEstadoCultivo_DescripcionEstadoCultivoAndAreaSembradaGreaterThan(
                            region, estadoBuscado, stockMinimo);
        } else if ((region == null || region.trim().isEmpty()) && productoId != null) {
            return cultivoRepository
                    .findByProductoVariedad_Producto_IdAndEstadoCultivo_DescripcionEstadoCultivoAndAreaSembradaGreaterThan(
                            productoId, estadoBuscado, stockMinimo);
        } else {
            return cultivoRepository
                    .findByAgricultor_UbicacionContainingIgnoreCaseAndProductoVariedad_Producto_IdAndEstadoCultivo_DescripcionEstadoCultivoAndAreaSembradaGreaterThan(
                            region, productoId, estadoBuscado, stockMinimo);
        }
    }

    // 2. LÓGICA TRANSACCIONAL PARA GESTIONAR EL PEDIDO Y REDUCIR STOCK
    @Transactional
    public Pedido crearPedido(PedidoRequestDTO request) {
        // Inicializar cabecera del Pedido
        Pedido pedido = new Pedido();
        pedido.setFechaCreacion(LocalDateTime.now());

        Comprador comprador = new Comprador();
        comprador.setId(request.getCompradorId());
        pedido.setComprador(comprador);

        Estado_Pedido estadoInicial = new Estado_Pedido();
        estadoInicial.setIdEstadoPedido(1L); 
        pedido.setEstadoPedido(estadoInicial);

        // Guardamos la cabecera
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Procesar cada elemento del carrito enviado por React
        for (PedidoRequestDTO.ItemCarritoDTO item : request.getItems()) {

            // Buscar el cultivo actual en el inventario
            Cultivo cultivo = cultivoRepository.findById(item.getCultivoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Error: El cultivo con ID " + item.getCultivoId() + " no existe."));

            // Validar stock disponible
            if (cultivo.getAreaSembrada() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el cultivo de: "
                        + cultivo.getProductoVariedad().getNombreProductoVariedad());
            }

            cultivo.setAreaSembrada(cultivo.getAreaSembrada() - item.getCantidad());
            cultivoRepository.save(cultivo);

        }

        return pedidoGuardado;
    }
}