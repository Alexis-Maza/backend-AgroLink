package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.PedidoRequestDTO;
import AgroLink.AgroLink.domain.dto.PedidoResponseDTO;
import AgroLink.AgroLink.domain.exception.StockInsuficienteException;
import AgroLink.AgroLink.domain.service.CatalogoPedidoService;
import AgroLink.AgroLink.persistance.entity.Cultivo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CatalogoPedidoController {

    private final CatalogoPedidoService catalogoPedidoService;

    // Catálogo público
    @GetMapping("/public/catalogo")
    public ResponseEntity<List<Cultivo>> obtenerCatalogo(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) Long productoId) {

        return ResponseEntity.ok(
            catalogoPedidoService.obtenerCatalogoAvanzado(search, region, precioMax, productoId)
        );
    }

    // Crear pedido - solo comprador autenticado
    @PostMapping("/comprador/pedidos/masivo")
    public ResponseEntity<?> crearPedido(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PedidoRequestDTO request) {
        try {
            PedidoResponseDTO response = catalogoPedidoService.crearPedido(
                    userDetails.getUsername(), request
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (StockInsuficienteException e) {
            Map<String, Object> body = new HashMap<>();
            body.put("mensaje", "Stock insuficiente");
            body.put("nombreProducto", e.getNombreProducto());
            body.put("stockDisponible", e.getStockDisponible());
            body.put("unidad", e.getUnidad());
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}