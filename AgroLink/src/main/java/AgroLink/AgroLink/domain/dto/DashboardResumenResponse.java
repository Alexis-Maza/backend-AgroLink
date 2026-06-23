package AgroLink.AgroLink.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO de respuesta para el endpoint GET /agricultor/dashboard.
 * Expone las métricas clave que el Frontend necesita para el panel de control.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResumenResponse {

    /** Desglose de conteos de cultivos según su estado actual. */
    private ResumenCultivos resumen_cultivos;

    /**
     * Sumatoria de 'cantidad_disponible' de todos los cultivos del agricultor.
     * Representa el volumen de stock disponible en kilogramos (o la unidad configurada).
     */
    private BigDecimal volumen_total_disponible_kg;

    /**
     * Suma del 'precio_pactado' de todos los DetallePedido vinculados
     * a cultivos del agricultor, cuyo pedido padre fue creado en el mes en curso.
     * Expresado en Soles (PEN).
     */
    private BigDecimal ventas_mes_actual_pen;

    // ── Clase interna para el resumen de cultivos ─────────────────────────

    /**
     * Contadores de cultivos según la descripción de su Estado_Cultivo.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenCultivos {

        /** Cultivos cuyo estado NO es "Cosechado" (todos los que siguen activos). */
        private long activos;

        /** Cultivos en estado "En crecimiento". */
        private long en_maduracion;

        /** Cultivos en estado "Listo para cosechar". */
        private long por_cosechar;
    }
}
