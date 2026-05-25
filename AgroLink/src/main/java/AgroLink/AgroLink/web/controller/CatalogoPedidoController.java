package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.PedidoRequestDTO;
import AgroLink.AgroLink.domain.service.CatalogoPedidoService;
import AgroLink.AgroLink.persistance.entity.Cultivo;
import AgroLink.AgroLink.persistance.entity.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/public")
@CrossOrigin(origins = "*") 
public class CatalogoPedidoController {

    @Autowired
    private CatalogoPedidoService catalogoPedidoService;

    @GetMapping("/catalogo")
    public ResponseEntity<List<Cultivo>> obtenerCatalogo(
            @RequestParam(required = false, name = "search") String search, 
            @RequestParam(required = false) String region,       
            @RequestParam(required = false) Double precioMax,  
            @RequestParam(required = false) Long productoId) {
        
        // Llamamos al método avanzado del servicio pasándole los 4 filtros cruzados
        List<Cultivo> catalogo = catalogoPedidoService.obtenerCatalogoAvanzado(search, region, precioMax, productoId);
        return new ResponseEntity<>(catalogo, HttpStatus.OK);
    }

    @PostMapping("/pedidos")
    public ResponseEntity<?> crearPedido(@RequestBody PedidoRequestDTO request) {
        try {
            Pedido nuevoPedido = catalogoPedidoService.crearPedido(request);
            return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}