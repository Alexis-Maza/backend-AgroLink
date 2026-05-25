package AgroLink.AgroLink.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO de respuesta para una entrada del historial de cultivo.
 * Incluye la información de retraso calculada por el scheduler.
 */
@Data
@AllArgsConstructor
public class HistorialCultivoResponse {

    private Long id;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Días reales transcurridos desde el inicio de esta etapa
    private Long diasTranscurridos;

    // Días de duración estimada según el estándar de la variedad
    private Integer diasDuracionEstimada;

    // Indica si hay alerta por superar el 20% del tiempo estimado
    private Boolean alertaRetraso;

    // Porcentaje de retraso calculado (negativo si va adelantado)
    private Double porcentajeRetraso;

    // Descripción del estado del cultivo en esta etapa
    private String estadoCultivo;
}
