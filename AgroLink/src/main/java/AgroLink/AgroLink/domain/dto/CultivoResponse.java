package AgroLink.AgroLink.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de respuesta con los datos de un cultivo.
 */
@Data
@AllArgsConstructor
public class CultivoResponse {

    private Long id;
    private LocalDate fechaSiembra;
    private BigDecimal areaSembrada;

    // Descripción del estado actual (de Estado_Cultivo.descripcionEstadoCultivo)
    private String estadoCultivo;

    // Indicador de alerta por retraso en la etapa activa
    private Boolean alertaRetraso;

    // Información de la variedad de producto
    private Long idProductoVariedad;
    private String nombreProducto;
    private String nombreProductoVariedad;

    // Días totales estimados para el ciclo
    private Integer diasTotalesEstimados;

    // Disponibilidad en catálogo
    private Boolean disponible;

    // Campos nuevos
    private String lote;
    private BigDecimal precio;
    private BigDecimal minimoVenta;
    private BigDecimal cantidadEstimada;
    private BigDecimal cantidadDisponible;
    private String unidad;
    private String imagenUrl;

    // RF-AF-07: disponible + lo reservado en pedidos activos (Pendiente/En preparación) de este cultivo.
    // Es el tope real hasta el que se puede registrar una merma.
    private BigDecimal stockTotalRestante;
}
