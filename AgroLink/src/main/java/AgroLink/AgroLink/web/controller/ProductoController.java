package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.ProductoResponse;
import AgroLink.AgroLink.domain.dto.ProductoVariedadResponse;
import AgroLink.AgroLink.domain.repository.ProductoRepository;
import AgroLink.AgroLink.domain.repository.ProductoVariedadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final ProductoVariedadRepository productoVariedadRepository;

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listarProductos() {
        List<ProductoResponse> productos = productoRepository.findAll()
                .stream()
                .map(p -> new ProductoResponse(p.getId(), p.getNombre()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{idProducto}/variedades")
    public ResponseEntity<List<ProductoVariedadResponse>> listarVariedades(
            @PathVariable Long idProducto) {
        List<ProductoVariedadResponse> variedades = productoVariedadRepository
                .findByProductoIdAndActivoTrue(idProducto) // ← cambiar esto
                .stream()
                .map(v -> new ProductoVariedadResponse(v.getId(), v.getNombreProductosVariedad()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(variedades);
    }
}