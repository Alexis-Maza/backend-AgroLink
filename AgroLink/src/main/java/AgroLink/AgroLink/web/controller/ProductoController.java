package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.ProductoCatalogoResponse;
import AgroLink.AgroLink.domain.dto.ProductoResponse;
import AgroLink.AgroLink.domain.dto.ProductoVariedadResponse;
import AgroLink.AgroLink.domain.repository.*;
import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Producto_Variedad;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoRepository productoRepository;
    private final ProductoVariedadRepository productoVariedadRepository;
    private final CultivoRepository cultivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgricultorRepository agricultorRepository;

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

    @GetMapping("/catalogo")
    public ResponseEntity<List<ProductoCatalogoResponse>> getCatalogo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Agricultor agricultor = agricultorRepository.findByUsuarioEmail(username)
                .orElseThrow(() -> new RuntimeException("Agricultor no encontrado"));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<ProductoCatalogoResponse> catalogo = productoRepository.findAll()
                .stream()
                .map(producto -> {
                    List<Producto_Variedad> variedades = productoVariedadRepository
                            .findByProductoIdAndActivoTrue(producto.getId());

                    List<ProductoCatalogoResponse.VariedadConCultivosResponse> varResp = variedades.stream()
                            .map(variedad -> {
                                // Solo cultivos DEL agricultor autenticado
                                List<ProductoCatalogoResponse.CultivoResumenResponse> cultivosResp =
                                        cultivoRepository.findByProductoVariedadIdAndAgricultorId(
                                                        variedad.getId(), agricultor.getId())
                                                .stream()
                                                .map(c -> new ProductoCatalogoResponse.CultivoResumenResponse(
                                                        c.getId(),
                                                        c.getLote(),
                                                        c.getEstadoCultivo().getDescripcionEstadoCultivo(),
                                                        c.getFechaSiembra().format(fmt),
                                                        c.getFechaSiembra().plusDays(c.getDiasTotalesEstimados()).format(fmt),
                                                        c.getCantidadEstimada() != null ? c.getCantidadEstimada().doubleValue() : 0,
                                                        c.getCantidadDisponible() != null ? c.getCantidadDisponible().doubleValue() : 0,
                                                        c.getUnidad(),
                                                        c.getPrecio() != null ? c.getPrecio().doubleValue() : 0,
                                                        c.getAreaSembrada() != null ? c.getAreaSembrada().doubleValue() : 0
                                                ))
                                                .collect(Collectors.toList());

                                return cultivosResp.isEmpty() ? null :  // ← excluir variedades vacías
                                        new ProductoCatalogoResponse.VariedadConCultivosResponse(
                                                variedad.getId(),
                                                variedad.getNombreProductosVariedad(),
                                                cultivosResp
                                        );
                            })
                            .filter(v -> v != null) // ← eliminar nulls
                            .collect(Collectors.toList());

                    return varResp.isEmpty() ? null : // ← excluir productos vacíos
                            new ProductoCatalogoResponse(producto.getId(), producto.getNombre(), varResp);
                })
                .filter(p -> p != null) // ← eliminar nulls
                .collect(Collectors.toList());

        return ResponseEntity.ok(catalogo);
    }
}