package AgroLink.AgroLink.domain.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * DTO para registrar el inicio de una nueva etapa en el historial de un cultivo.
 */
@Data
public class HistorialCultivoRequest {

    private LocalDate fechaInicio;

    // ID de la etapa de referencia de la variedad (para calcular el retraso)
    private Long idEtapaProductoVariedad;
}
