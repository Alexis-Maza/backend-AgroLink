package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.ProductoDTO;
import AgroLink.AgroLink.domain.dto.ProductoRequestDTO;
import AgroLink.AgroLink.domain.dto.ProductoVariedadDTO;
import AgroLink.AgroLink.domain.dto.ProductoVariedadRequestDTO;
import AgroLink.AgroLink.persistance.entity.Producto;
import AgroLink.AgroLink.persistance.entity.Producto_Variedad;
import AgroLink.AgroLink.domain.repository.ProductoRepository;
import AgroLink.AgroLink.domain.repository.ProductoVariedadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoVariedadRepository productoVariedadRepository;

    // ─── PRODUCTOS ────────────────────────────────────────────────

    public List<ProductoDTO> listarProductos() {
        return productoRepository.findAll().stream()
                .map(this::toProductoDTO)
                .collect(Collectors.toList());
    }

    public ProductoDTO crearProducto(ProductoRequestDTO request) {
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        return toProductoDTO(productoRepository.save(producto));
    }

    public ProductoDTO actualizarProducto(Long id, ProductoRequestDTO request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        producto.setNombre(request.getNombre());
        return toProductoDTO(productoRepository.save(producto));
    }

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }
        productoRepository.deleteById(id);
    }

    public ProductoVariedadDTO toggleEstadoVariante(Long id) {
        Producto_Variedad variedad = productoVariedadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada con id: " + id));
        variedad.setActivo(!variedad.isActivo());
        return toVariedadDTO(productoVariedadRepository.save(variedad));
    }

    // ─── VARIANTES ────────────────────────────────────────────────

    public List<ProductoVariedadDTO> listarVariantesPorProducto(Long idProducto) {
        return productoVariedadRepository.findByProductoId(idProducto).stream()
                .map(this::toVariedadDTO)
                .collect(Collectors.toList());
    }

    public List<ProductoVariedadDTO> listarTodasVariantes() {
        return productoVariedadRepository.findAll().stream()
                .map(this::toVariedadDTO)
                .collect(Collectors.toList());
    }

    public ProductoVariedadDTO crearVariante(ProductoVariedadRequestDTO request) {
        Producto producto = productoRepository.findById(request.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + request.getIdProducto()));

        Producto_Variedad variedad = new Producto_Variedad();
        variedad.setNombreProductosVariedad(request.getNombreProductosVariedad());
        variedad.setProducto(producto);
        variedad.setActivo(true);

        return toVariedadDTO(productoVariedadRepository.save(variedad));
    }

    public ProductoVariedadDTO actualizarVariante(Long id, ProductoVariedadRequestDTO request) {
        Producto_Variedad variedad = productoVariedadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variante no encontrada con id: " + id));

        Producto producto = productoRepository.findById(request.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + request.getIdProducto()));

        variedad.setNombreProductosVariedad(request.getNombreProductosVariedad());
        variedad.setProducto(producto);

        return toVariedadDTO(productoVariedadRepository.save(variedad));
    }

    public void eliminarVariante(Long id) {
        if (!productoVariedadRepository.existsById(id)) {
            throw new RuntimeException("Variante no encontrada con id: " + id);
        }
        productoVariedadRepository.deleteById(id);
    }

    // ─── MAPPERS ──────────────────────────────────────────────────

    private ProductoDTO toProductoDTO(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setActivo(true);

        // Obtener la lista completa de variantes
        List<Producto_Variedad> variantes = productoVariedadRepository.findByProductoId(p.getId());
        dto.setCantidadVariantes(variantes.size());   // conteo desde la lista
        dto.setVariedades(                            // lista con id y nombre
                variantes.stream()
                        .map(this::toVariedadDTO)
                        .collect(Collectors.toList())
        );

        return dto;
    }

    private ProductoVariedadDTO toVariedadDTO(Producto_Variedad v) {
        ProductoVariedadDTO dto = new ProductoVariedadDTO();
        dto.setId(v.getId());
        dto.setNombreProductosVariedad(v.getNombreProductosVariedad());
        dto.setActivo(v.isActivo());
        if (v.getProducto() != null) {
            dto.setIdProducto(v.getProducto().getId());
            dto.setNombreProducto(v.getProducto().getNombre());
        }
        return dto;
    }
}