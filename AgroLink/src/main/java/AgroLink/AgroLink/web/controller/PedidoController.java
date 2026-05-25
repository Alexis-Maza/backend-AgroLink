package AgroLink.AgroLink.web.controller; // ⚠️ Ajusta este paquete según tu estructura (ej: domain.controller o web)

import AgroLink.AgroLink.domain.dto.PedidoRequestDTO;
import AgroLink.AgroLink.domain.service.CatalogoPedidoService;
import AgroLink.AgroLink.persistance.entity.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/pedidos") // 🟢 Concuerda con la ruta de tu React
@CrossOrigin(origins = "*") // Evita bloqueos de CORS en desarrollo
public class PedidoController {

    @Autowired
    private CatalogoPedidoService catalogoPedidoService;

    @PostMapping("/masivo") // 🟢 Al unirse con el de arriba forma /public/pedidos/masivo
    public ResponseEntity<?> procesarPedidoMasivo(@RequestBody PedidoRequestDTO pedidoDTO) {
        try {
            // Llamamos a tu servicio transaccional que ya calcula hectáreas y reduce stock
            Pedido pedidoGuardado = catalogoPedidoService.crearPedido(pedidoDTO);
            return ResponseEntity.ok(pedidoGuardado);
        } catch (RuntimeException e) {
            // Si falta stock, devolvemos un 400 Bad Request con el mensaje del error
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno en el servidor: " + e.getMessage());
        }
    }
}