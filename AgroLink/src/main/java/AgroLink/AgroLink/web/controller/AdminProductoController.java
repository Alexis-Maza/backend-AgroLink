package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.ProductoDTO;
import AgroLink.AgroLink.domain.dto.ProductoRequestDTO;
import AgroLink.AgroLink.domain.dto.ProductoVariedadDTO;
import AgroLink.AgroLink.domain.dto.ProductoVariedadRequestDTO;
import AgroLink.AgroLink.domain.service.AdminProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminProductoController {

    private final AdminProductoService adminProductoService;

    // ─── PRODUCTOS ────────────────────────────────────────────────

    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO>> listarProductos() {
        return ResponseEntity.ok(adminProductoService.listarProductos());
    }

    @PostMapping("/productos")
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminProductoService.crearProducto(request));
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @PathVariable Long id,
            @Valid @RequestBody ProductoRequestDTO request) {
        return ResponseEntity.ok(adminProductoService.actualizarProducto(id, request));
    }

    @DeleteMapping("/productos/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        adminProductoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // ─── VARIANTES ────────────────────────────────────────────────

    @GetMapping("/productos/variantes")
    public ResponseEntity<List<ProductoVariedadDTO>> listarTodasVariantes() {
        return ResponseEntity.ok(adminProductoService.listarTodasVariantes());
    }

    @GetMapping("/productos/{idProducto}/variantes")
    public ResponseEntity<List<ProductoVariedadDTO>> listarVariantesPorProducto(
            @PathVariable Long idProducto) {
        return ResponseEntity.ok(adminProductoService.listarVariantesPorProducto(idProducto));
    }

    @PostMapping("/productos/variantes")
    public ResponseEntity<ProductoVariedadDTO> crearVariante(
            @Valid @RequestBody ProductoVariedadRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminProductoService.crearVariante(request));
    }

    @PutMapping("/productos/variantes/{id}")
    public ResponseEntity<ProductoVariedadDTO> actualizarVariante(
            @PathVariable Long id,
            @Valid @RequestBody ProductoVariedadRequestDTO request) {
        return ResponseEntity.ok(adminProductoService.actualizarVariante(id, request));
    }

    @DeleteMapping("/productos/variantes/{id}")
    public ResponseEntity<Void> eliminarVariante(@PathVariable Long id) {
        adminProductoService.eliminarVariante(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/productos/variantes/{id}/toggle-estado")
    public ResponseEntity<ProductoVariedadDTO> toggleEstadoVariante(@PathVariable Long id) {
        return ResponseEntity.ok(adminProductoService.toggleEstadoVariante(id));
    }
}