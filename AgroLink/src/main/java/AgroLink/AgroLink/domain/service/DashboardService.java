package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.DashboardResumenResponse;
import AgroLink.AgroLink.domain.repository.AgricultorRepository;
import AgroLink.AgroLink.domain.repository.CultivoRepository;
import AgroLink.AgroLink.domain.repository.DetallePedidoRepository;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Cultivo;
import AgroLink.AgroLink.persistance.entity.DetallePedido;
import AgroLink.AgroLink.persistance.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de calcular las métricas del panel de control (Dashboard)
 * del agricultor autenticado.
 *
 * Reglas de negocio:
 * - "activos"       : cultivos cuyo estado NO sea "Cosechado"
 * - "en_maduracion" : cultivos con estado "En crecimiento"
 * - "por_cosechar"  : cultivos con estado "Listo para cosechar"
 * - Volumen         : suma de cantidadDisponible de todos sus cultivos
 * - Ventas del mes  : suma de precioPactado de DetallePedido cuyo Pedido
 *                     fue creado en el mes y año en curso
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    // Nombre exacto del estado tal como está almacenado en la tabla estado_cultivo
    private static final String ESTADO_COSECHADO       = "Cosechado";
    private static final String ESTADO_EN_CRECIMIENTO  = "En crecimiento";
    private static final String ESTADO_LISTO_COSECHAR  = "Listo para cosechar";

    private final UsuarioRepository        usuarioRepository;
    private final AgricultorRepository     agricultorRepository;
    private final CultivoRepository        cultivoRepository;
    private final DetallePedidoRepository  detallePedidoRepository;

    /**
     * Calcula y devuelve el resumen del dashboard para el agricultor
     * identificado por su email (extraído del JWT).
     *
     * @param email Email del usuario autenticado (obtenido de userDetails.getUsername()).
     * @return DashboardResumenResponse con todas las métricas calculadas.
     */
    public DashboardResumenResponse obtenerDashboard(String email) {

        // ── 1. Resolver entidad Agricultor desde el email del token ──────────
        Agricultor agricultor = obtenerAgricultorPorEmail(email);

        // ── 2. Traer todos los cultivos del agricultor ───────────────────────
        //      Usa el Query Method existente en CultivoRepository.
        List<Cultivo> cultivos = cultivoRepository.findByAgricultor(agricultor);

        // ── 3. Calcular métricas de cultivos con Streams (sin @Query) ────────
        long activos = cultivos.stream()
                .filter(c -> c.getEstadoCultivo() != null)
                .filter(c -> !ESTADO_COSECHADO.equals(
                        c.getEstadoCultivo().getDescripcionEstadoCultivo()))
                .count();

        long enMaduracion = cultivos.stream()
                .filter(c -> c.getEstadoCultivo() != null)
                .filter(c -> ESTADO_EN_CRECIMIENTO.equals(
                        c.getEstadoCultivo().getDescripcionEstadoCultivo()))
                .count();

        long porCosechar = cultivos.stream()
                .filter(c -> c.getEstadoCultivo() != null)
                .filter(c -> ESTADO_LISTO_COSECHAR.equals(
                        c.getEstadoCultivo().getDescripcionEstadoCultivo()))
                .count();

        // ── 4. Volumen total disponible: suma de cantidadDisponible ──────────
        BigDecimal volumenTotal = cultivos.stream()
                .filter(c -> c.getCantidadDisponible() != null)
                .map(Cultivo::getCantidadDisponible)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── 5. Ventas del mes actual ─────────────────────────────────────────
        //      Navegación: cultivos del agricultor
        //                  → DetallePedido.cultivo (findByCultivoIn)
        //                  → filtra por Pedido.fechaCreacion en el mes/año actual
        //                  → suma DetallePedido.precioPactado
        BigDecimal ventasMesActual = BigDecimal.ZERO;

        if (!cultivos.isEmpty()) {
            LocalDateTime ahora      = LocalDateTime.now();
            int           mesActual  = ahora.getMonthValue();
            int           anoActual  = ahora.getYear();

            // Trae todas las líneas de pedido vinculadas a los cultivos del agricultor
            List<DetallePedido> detalles = detallePedidoRepository.findByCultivoIn(cultivos);

            ventasMesActual = detalles.stream()
                    // Solo los del mes y año en curso (navegamos DetallePedido → Pedido → fechaCreacion)
                    .filter(d -> d.getPedido() != null
                              && d.getPedido().getFechaCreacion() != null
                              && d.getPedido().getFechaCreacion().getMonthValue() == mesActual
                              && d.getPedido().getFechaCreacion().getYear()       == anoActual)
                    // Solo detalles con precio válido
                    .filter(d -> d.getPrecioPactado() != null)
                    .map(DetallePedido::getPrecioPactado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // ── 6. Construir y retornar la respuesta ─────────────────────────────
        DashboardResumenResponse.ResumenCultivos resumenCultivos =
                DashboardResumenResponse.ResumenCultivos.builder()
                        .activos(activos)
                        .en_maduracion(enMaduracion)
                        .por_cosechar(porCosechar)
                        .build();

        return DashboardResumenResponse.builder()
                .resumen_cultivos(resumenCultivos)
                .volumen_total_disponible_kg(volumenTotal)
                .ventas_mes_actual_pen(ventasMesActual)
                .build();
    }

    // ── Método auxiliar ── mismo patrón que CultivoService ──────────────────

    /**
     * Resuelve el perfil Agricultor a partir del email del token JWT.
     * Sigue el mismo patrón ya establecido en CultivoService para consistencia.
     */
    private Agricultor obtenerAgricultorPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario no encontrado para el email: " + email));
        return agricultorRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException(
                        "Perfil de agricultor no encontrado para: " + email));
    }
}
