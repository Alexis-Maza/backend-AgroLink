package AgroLink.AgroLink.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * RF-TRAZABILIDAD: DTO de respuesta unificada para el endpoint
 * GET /api/v1/trazabilidad/{id}.
 *
 * Consolida en una sola respuesta:
 *   1. Datos del agricultor propietario del cultivo
 *   2. Datos del cultivo consultado
 *   3. Historial cronológico de etapas
 *   4. Pedidos vinculados a través de DetallePedido
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrazabilidadResponse {

    // ── 1. Datos del agricultor ───────────────────────────────────────────
    private AgricultorInfoDTO agricultor;

    // ── 2. Datos del cultivo ──────────────────────────────────────────────
    private CultivoInfoDTO cultivo;

    // ── 3. Historial de etapas ────────────────────────────────────────────
    private List<EtapaDTO> historialEtapas;

    // ── 4. Pedidos vinculados ─────────────────────────────────────────────
    private List<PedidoVinculadoDTO> pedidosVinculados;

    // ─────────────────────────────────────────────────────────────────────
    // Sub-DTOs internos
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Datos esenciales del agricultor dueño del cultivo.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgricultorInfoDTO {
        private Long idAgricultor;
        private String nombres;
        private String apellidoPaterno;
        private String apellidoMaterno;
        private String email;
        private String dniRuc;
        private String ubicacion;
        private BigDecimal hectareasTotales;
        private Integer anosExperiencia;
        private String certificaciones;
        private String fotoPerfil;
    }

    /**
     * Datos del cultivo consultado (lote + estado + producto).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CultivoInfoDTO {
        private Long idCultivo;
        private String lote;
        private LocalDate fechaSiembra;
        private BigDecimal areaSembrada;
        private Integer diasTotalesEstimados;
        private BigDecimal cantidadEstimada;
        private BigDecimal cantidadDisponible;
        private String unidad;
        private BigDecimal precio;
        private BigDecimal minimoVenta;
        private Boolean disponible;
        private String estadoActual;
        private String imagenUrl;

        // Observaciones generales del cultivo
        private String observaciones;

        // Producto asociado
        private String nombreProducto;
        private String nombreVariedad;
    }

    /**
     * Una etapa del historial cronológico del cultivo.
     * Incluye información de retraso calculada.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EtapaDTO {
        private Long idHistorial;
        private String nombreEtapa;
        private String estadoCultivo;
        private LocalDate fechaInicio;
        private LocalDate fechaFin;

        /** true si esta etapa sigue activa (sin fecha de fin). */
        private Boolean activa;

        /** Días reales transcurridos desde el inicio de la etapa. */
        private Long diasTranscurridos;

        /** Días estimados para esta etapa según el estándar de la variedad. */
        private Integer diasDuracionEstimada;

        /**
         * Porcentaje de retraso: positivo = retraso, negativo = adelantado.
         * null si no hay etapa de referencia asignada.
         */
        private Double porcentajeRetraso;

        /** true si el retraso supera el 20% del tiempo estimado. */
        private Boolean alertaRetraso;

        /** Observaciones del agricultor para esta etapa específica. */
        private String observaciones;
    }

    /**
     * Resumen de un pedido vinculado al cultivo.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PedidoVinculadoDTO {
        private Long idPedido;
        private LocalDateTime fechaCreacion;
        private String estadoPedido;

        // Comprador que realizó el pedido
        private String nombreComprador;
        private String emailComprador;
        private String nombreNegocio;

        // Detalle de este cultivo dentro del pedido
        private BigDecimal cantidadSolicitada;
        private BigDecimal precioPactado;
        private BigDecimal cantidadEntrega;
        private String direccionEntrega;
        private String unidadMedida;
    }
}
