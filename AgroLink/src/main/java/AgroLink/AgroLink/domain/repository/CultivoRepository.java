package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CultivoRepository extends JpaRepository<Cultivo, Long> {

    // Obtener todos los cultivos de un agricultor
    List<Cultivo> findByAgricultor(Agricultor agricultor);

    // Obtener cultivos de un agricultor ordenados por fecha de siembra
    List<Cultivo> findByAgricultorOrderByFechaSiembraDesc(Agricultor agricultor);

    // Obtener cultivos activos basados en una lista de descripciones de estado (Query Method nativo)
    List<Cultivo> findByEstadoCultivoDescripcionEstadoCultivoIn(java.util.Collection<String> descripciones);

    // Obtener todos los cultivos disponibles para preventa (Query Method nativo)
    List<Cultivo> findByDisponibleTrue();
}
