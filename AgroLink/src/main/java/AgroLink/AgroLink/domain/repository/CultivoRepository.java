package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    // ── Métodos del agricultor ──
    List<Cultivo> findByAgricultor(Agricultor agricultor);
    List<Cultivo> findByAgricultorOrderByFechaSiembraDesc(Agricultor agricultor);
    List<Cultivo> findByEstadoCultivoDescripcionEstadoCultivoIn(java.util.Collection<String> descripciones);
    List<Cultivo> findByDisponibleTrue();

    // ── Filtro avanzado del catálogo del comprador ──
    @Query("SELECT c FROM Cultivo c WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(c.productoVariedad.nombreProductosVariedad) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:region IS NULL OR :region = '' OR LOWER(c.agricultor.ubicacion) LIKE LOWER(CONCAT('%', :region, '%'))) AND " +
           "(:precioMax IS NULL OR c.precio <= :precioMax) AND " +
           "(:productoId IS NULL OR c.productoVariedad.producto.id = :productoId) AND " +
           "c.cantidadDisponible > 0 AND c.disponible = true")
    List<Cultivo> filtrarCatalogoAvanzado(
            @Param("search") String search,
            @Param("region") String region,
            @Param("precioMax") Double precioMax,
            @Param("productoId") Long productoId);
}