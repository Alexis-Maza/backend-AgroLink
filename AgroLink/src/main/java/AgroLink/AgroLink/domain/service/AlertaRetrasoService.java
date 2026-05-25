package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.repository.HistorialCultivoRepository;
import AgroLink.AgroLink.persistance.entity.Historial_Cultivo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * RF-A2-05: Servicio de alertas automáticas por retraso en las etapas de cultivo.
 *
 * Regla de negocio: Si los días reales transcurridos en la etapa actual superan
 * en más de un 20% los días estimados definidos en Etapa_Producto_Variedad.
 *
 * Fórmula: diasTranscurridos > diasDuracionEstimada * 1.20
 */
@Service
@RequiredArgsConstructor
public class AlertaRetrasoService {

    private final HistorialCultivoRepository historialCultivoRepository;

    /**
     * Tarea programada que evalúa diariamente las alertas de retraso.
     * Se ejecuta todos los días a las 6:00 AM (hora del servidor).
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void verificarAlertasDeRetraso() {
        System.out.println("[AlertaRetrasoService] Iniciando verificación de retrasos: "
                + LocalDate.now());

        // Obtener todas las etapas activas (sin fecha de fin) que tienen referencia estándar
        List<Historial_Cultivo> etapasActivas =
                historialCultivoRepository.findByFechaFinIsNullAndEtapaProductoVariedadIsNotNull();

        long conAlerta = etapasActivas.stream()
                .filter(this::calcularSiHayRetraso)
                .count();

        System.out.println("[AlertaRetrasoService] Verificación completada. "
                + "Etapas con retraso detectadas: " + conAlerta
                + " | Total evaluadas: " + etapasActivas.size());
    }

    /**
     * Calcula si una etapa del historial tiene retraso mayor al 20%.
     *
     * @param historial Entrada del historial con etapa activa
     * @return true si los días reales superan en más del 20% los días estimados
     */
    public boolean calcularSiHayRetraso(Historial_Cultivo historial) {
        if (historial.getEtapaProductoVariedad() == null) {
            return false;
        }

        Integer diasDuracionEstimada = historial.getEtapaProductoVariedad().getDiasDuracionEstimada();
        if (diasDuracionEstimada == null || diasDuracionEstimada <= 0) {
            return false;
        }

        long diasTranscurridos = ChronoUnit.DAYS.between(
                historial.getFechaInicio(),
                LocalDate.now()
        );

        // Umbral: días de duración estimada + 20%
        double umbralRetraso = diasDuracionEstimada * 1.20;

        return diasTranscurridos > umbralRetraso;
    }

    /**
     * Permite ejecutar la verificación manualmente (útil para pruebas o endpoints admin).
     */
    public String ejecutarVerificacionManual() {
        List<Historial_Cultivo> etapasActivas =
                historialCultivoRepository.findByFechaFinIsNullAndEtapaProductoVariedadIsNotNull();

        long conAlerta = etapasActivas.stream()
                .filter(this::calcularSiHayRetraso)
                .count();

        return "Verificación manual completada. Etapas con retraso: " + conAlerta
                + " de " + etapasActivas.size() + " evaluadas.";
    }
}
