package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    // 1. Buscar solo por Estado y con stock disponible (Cuando no se aplican
    // filtros de búsqueda)
    List<Cultivo> findByEstadoCultivo_DescripcionEstadoCultivoAndAreaSembradaGreaterThan(
            String estado,
            Double stockMinimo);

    // 2. Filtrar por Región, Estado y Stock (Cuando el usuario filtra solo por
    // ubicación)
    List<Cultivo> findByAgricultor_UbicacionContainingIgnoreCaseAndEstadoCultivo_DescripcionEstadoCultivoAndAreaSembradaGreaterThan(
            String region,
            String estado,
            Double stockMinimo);

    // 3. Filtrar por Producto, Estado y Stock (Cuando el usuario filtra solo por
    // tipo de alimento)
    List<Cultivo> findByProductoVariedad_Producto_IdAndEstadoCultivo_DescripcionEstadoCultivoAndAreaSembradaGreaterThan(
            Long productoId,
            String estado,
            Double stockMinimo);

    // 4. Filtro Avanzado Completo: Región + Producto + Estado + Stock (Cuando usa
    // ambos filtros a la vez)
    List<Cultivo> findByAgricultor_UbicacionContainingIgnoreCaseAndProductoVariedad_Producto_IdAndEstadoCultivo_DescripcionEstadoCultivoAndAreaSembradaGreaterThan(
            String region,
            Long productoId,
            String estado,
            Double stockMinimo);
}