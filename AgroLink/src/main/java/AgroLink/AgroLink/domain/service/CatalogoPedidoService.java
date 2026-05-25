package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.PedidoRequestDTO;
import AgroLink.AgroLink.domain.repository.CultivoRepository;
import AgroLink.AgroLink.domain.repository.PedidoRepository;
import AgroLink.AgroLink.persistance.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogoPedidoService {

    @Autowired
    private CultivoRepository cultivoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public List<Cultivo> obtenerCatalogoAvanzado(String search, String region, Double precioMax, Long productoId) {
        // Si los textos vienen vacíos "" o con puros espacios, los pasamos como null para la @Query
        String filtroSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String filtroRegion = (region != null && !region.trim().isEmpty()) ? region.trim() : null;
        
        // Si el precio máximo es 0 o menor, no limita el catálogo (se vuelve null)
        Double filtroPrecio = (precioMax != null && precioMax > 0) ? precioMax : null;

        // Conectamos con el repositorio dinámico
        return cultivoRepository.filtrarCatalogoAvanzado(filtroSearch, filtroRegion, filtroPrecio, productoId);
    }

    // 2. LÓGICA TRANSACCIONAL PARA GESTIONAR EL PEDIDO Y REDUCIR STOCK
    @Transactional
    public Pedido crearPedido(PedidoRequestDTO request) {
        // 1. Inicializar cabecera del Pedido (Maestro)
        Pedido pedido = new Pedido();
        pedido.setFechaCreacion(LocalDateTime.now());

        Comprador comprador = new Comprador();
        comprador.setId(request.getCompradorId() != null ? request.getCompradorId() : 1L); // Fallback por seguridad
        pedido.setComprador(comprador);

        Estado_Pedido estadoInicial = new Estado_Pedido();
        estadoInicial.setIdEstadoPedido(1L); // 1 = Pendiente
        pedido.setEstadoPedido(estadoInicial);

        // Lista temporal para ir acumulando los detalles del carrito
        List<DetallePedido> detallesLista = new ArrayList<>();

        // 2. Recorrer cada ítem enviado desde el carrito de React
        for (PedidoRequestDTO.ItemCarritoDTO item : request.getItems()) {

            // Buscar el cultivo actual en la base de datos (tabla 'cultivos')
            Cultivo cultivo = cultivoRepository.findById(item.getCultivoId())
                    .orElseThrow(() -> new RuntimeException(
                            "Error: El cultivo con ID " + item.getCultivoId() + " no existe."));

            // 🟢 CONVERSIÓN DINÁMICA: Kilos ingresados por el usuario a Hectáreas equivalentes
            // Regla de negocio: 10,000 Kg equivalen a 1 Hectárea de campo
            Double hectareasAComprar = item.getCantidad() / 10000.0;

            // Validar stock disponible (Exigencia RF-A2-04) usando las hectáreas equivalentes
            if (cultivo.getAreaSembrada() < hectareasAComprar) {
                throw new RuntimeException("Stock insuficiente para el cultivo de: "
                        + cultivo.getProductoVariedad().getNombreProductoVariedad()
                        + ". Disponible en el campo: " + cultivo.getAreaSembrada() + " Ha.");
            }

            // 📉 Descontar las Hectáreas equivalentes del área sembrada en PostgreSQL en tiempo real
            cultivo.setAreaSembrada(cultivo.getAreaSembrada() - hectareasAComprar);
            cultivoRepository.save(cultivo);

            // 📝 Crear la instancia real del detalle del pedido
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido); // Vinculamos el detalle a esta cabecera única
            detalle.setCultivo(cultivo);
            detalle.setCantidadSolicitada(item.getCantidad()); // Guarda los Kilos solicitados para la boleta
            detalle.setPrecioPactado(item.getPrecioPactado());
            detalle.setCantidadEntrega(0.0); // Comienza en 0 por ser preventa
            detalle.setDireccion(item.getDireccionEntrega());

            // Configurar la unidad de medida obligatoria que pide tu entidad
            UnidadMedidaProducto ump = new UnidadMedidaProducto();
            ump.setIdUnidadMedidaProducto(request.getIdUnidadMedida() != null ? request.getIdUnidadMedida() : 1L);
            detalle.setUnidadMedidaProducto(ump);

            // Agregamos el ítem procesado a la lista
            detallesLista.add(detalle);
        }

        // 3. Inyectamos la lista de detalles completa a la cabecera
        pedido.setDetalles(detallesLista);

        // 💾 Guardamos el pedido completo en PostgreSQL
        return pedidoRepository.save(pedido);
    }
}