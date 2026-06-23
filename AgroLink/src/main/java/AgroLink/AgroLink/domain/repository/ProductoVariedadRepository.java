package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Producto_Variedad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoVariedadRepository extends JpaRepository<Producto_Variedad, Long> {
    List<Producto_Variedad> findByProductoId(Long idProducto);
        int countByProductoId(Long idProducto);
}