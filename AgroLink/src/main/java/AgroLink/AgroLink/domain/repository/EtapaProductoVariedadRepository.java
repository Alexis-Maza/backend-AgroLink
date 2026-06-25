package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Etapa_Producto_Variedad;
import AgroLink.AgroLink.persistance.entity.Producto_Variedad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtapaProductoVariedadRepository extends JpaRepository<Etapa_Producto_Variedad, Long> {

    // Obtener todas las etapas de una variedad ordenadas por su nombre
    List<Etapa_Producto_Variedad> findByProductoVariedad(Producto_Variedad productoVariedad);

    // Buscar una etapa específica por nombre dentro de una variedad
    Optional<Etapa_Producto_Variedad> findByProductoVariedadAndNombreEtapaProductosVariedad(
            Producto_Variedad productoVariedad, String nombreEtapaProductosVariedad);
}
