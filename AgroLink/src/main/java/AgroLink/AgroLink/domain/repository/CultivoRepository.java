package AgroLink.AgroLink.domain.repository;

import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Cultivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CultivoRepository extends JpaRepository<Cultivo, Long>,
                                           JpaSpecificationExecutor<Cultivo> {
    List<Cultivo> findByAgricultor(Agricultor agricultor);
    List<Cultivo> findByAgricultorId(Long agricultorId);
    List<Cultivo> findByAgricultorOrderByFechaSiembraDesc(Agricultor agricultor);
    List<Cultivo> findByEstadoCultivoDescripcionEstadoCultivoIn(
        java.util.Collection<String> descripciones);
    List<Cultivo> findByEstadoCultivoDescripcionEstadoCultivoNotIn(
        java.util.Collection<String> descripciones);
    List<Cultivo> findByDisponibleTrue();
    List<Cultivo> findByProductoVariedadId(Long idProductoVariedad);
    List<Cultivo> findByProductoVariedadIdAndAgricultorId(Long idVariedad, Long idAgricultor);
    // Métodos para reportes
    List<Cultivo> findByAgricultorAndFechaSiembraBetween(
            Agricultor agricultor, LocalDate fechaInicio, LocalDate fechaFin);
}