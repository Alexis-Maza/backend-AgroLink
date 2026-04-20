package AgroLink.AgroLink.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @GetMapping("/listado")
    public ResponseEntity<List<String>> getListadoCompleto() {
        return ResponseEntity.ok(generarListaSimulada());
    }

    @GetMapping("/categoria/{nombre}")
    public ResponseEntity<List<String>> getPorCategoria(@PathVariable String nombre) {
        // Lógica para filtrar (simulada)
        return ResponseEntity.ok(generarListaSimulada());
    }

    @GetMapping("/detalle/{id}")
    public ResponseEntity<String> getDetalle(@PathVariable Long id) {
        return ResponseEntity.ok("Información técnica del producto: " + id);
    }

    // Método privado para mejorar la mantenibilidad y encapsular la lógica de datos
    private List<String> generarListaSimulada() {
        return Arrays.asList("Frutas de temporada", "Hortalizas frescas", "Semillas");
    }
}
