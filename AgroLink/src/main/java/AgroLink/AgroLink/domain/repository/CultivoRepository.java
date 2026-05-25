package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    @Query("SELECT c FROM Cultivo c WHERE " +
           // 1. Filtro por Nombre de Producto (Corregido: quitamos la propiedad inexistente)
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(c.productoVariedad.nombreProductoVariedad) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +

           // 2. Filtro por Región (Ubicación del agricultor)
           "(:region IS NULL OR :region = '' OR LOWER(c.agricultor.ubicacion) LIKE LOWER(CONCAT('%', :region, '%'))) AND " +

           // 3. Filtro por Rango de Precio Máximo
           "(:precioMax IS NULL OR c.productoVariedad.precioProductoVariedad <= :precioMax) AND " +

           // 4. Filtro por Categoría / ID de Producto (Filtro por Tipo fijo)
           "(:productoId IS NULL OR c.productoVariedad.producto.id = :productoId) AND " +

           // 5. Regla de Negocio obligatoria: Solo cultivos con stock disponible
           "c.areaSembrada > 0")
    List<Cultivo> filtrarCatalogoAvanzado(
            @Param("search") String search,
            @Param("region") String region,
            @Param("precioMax") Double precioMax,
            @Param("productoId") Long productoId);
}