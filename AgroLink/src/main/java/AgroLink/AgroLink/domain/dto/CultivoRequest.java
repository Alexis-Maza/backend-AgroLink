package AgroLink.AgroLink.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para registrar o actualizar un cultivo.
 */
@Data
public class CultivoRequest {

    private LocalDate fechaSiembra;
    private BigDecimal areaSembrada;

    // ID del estado del cultivo (PLANIFICADO, EN_CURSO, etc.)
    private Long idEstadoCultivo;

    // ID de la variedad del producto sembrado
    private Long idProductoVariedad;

    // Días totales estimados para todo el ciclo del cultivo
    private Integer diasTotalesEstimados;
}
