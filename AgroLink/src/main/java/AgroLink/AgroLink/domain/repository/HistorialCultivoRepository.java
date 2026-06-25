package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Cultivo;
import AgroLink.AgroLink.persistance.entity.Historial_Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistorialCultivoRepository extends JpaRepository<Historial_Cultivo, Long> {

    // Obtener todo el historial de un cultivo ordenado cronológicamente
    List<Historial_Cultivo> findByCultivoOrderByFechaInicioAsc(Cultivo cultivo);

    // Obtener la etapa activa actual de un cultivo (sin fecha de fin)
    Optional<Historial_Cultivo> findByCultivoAndFechaFinIsNull(Cultivo cultivo);

    // RF-A2-05: Obtener todas las etapas activas que tienen etapa de referencia definida.
    // Equivalente a: WHERE fecha_fin IS NULL AND id_etapa_productos_variedad IS NOT NULL
    List<Historial_Cultivo> findByFechaFinIsNullAndEtapaProductoVariedadIsNotNull();

    void deleteAllByCultivo(Cultivo cultivo);
}
