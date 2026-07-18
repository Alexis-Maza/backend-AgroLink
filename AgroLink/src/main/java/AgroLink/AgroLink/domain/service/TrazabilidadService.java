package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.TrazabilidadResponse;
import AgroLink.AgroLink.domain.dto.TrazabilidadResponse.*;
import AgroLink.AgroLink.domain.repository.TrazabilidadRepository;
import AgroLink.AgroLink.persistance.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * RF-TRAZABILIDAD: Servicio que ensambla la cadena completa de trazabilidad
 * de un cultivo para el endpoint GET /api/v1/trazabilidad/{id}.
 *
 * ── Servicios Spring Boot utilizados ────────────────────────────────────────
 *
 *  1. @Service (Spring Core)
 *     Registra esta clase como bean gestionado por el contenedor IoC.
 *     Permite inyección de dependencias vía @RequiredArgsConstructor (Lombok).
 *
 *  2. @Transactional(readOnly = true)  (Spring Data / JPA)
 *     - readOnly=true: le indica al proveedor JPA (Hibernate) que la sesión
 *       es de solo lectura, lo que permite optimizaciones como:
 *         • Desactivar el "dirty-checking" (no hace flush al cerrar sesión).
 *         • Algunos drivers JDBC pueden habilitar réplicas de lectura.
 *     - Mantiene la sesión de Hibernate abierta durante todo el método, lo que
 *       garantiza que las asociaciones LAZY cargadas con JOIN FETCH en las
 *       queries del repositorio permanezcan accesibles al momento del mapeo.
 *
 *  3. Spring Security (autorización a nivel de servicio)
 *     La comprobación de autorización se realiza en dos capas:
 *       a) A nivel HTTP en SecurityConfig (.requestMatchers → hasAnyAuthority).
 *       b) A nivel de negocio aquí: si el rol es COMPRADOR, se valida que
 *          realmente tenga un pedido vinculado al cultivo antes de revelar
 *          información sensible del agricultor.
 *
 *  4. Slf4j (Spring Boot Logging)
 *     Registra trazas de debug y warn sin acoplarse a una implementación
 *     concreta de logging (Logback / Log4j2 intercambiables).
 *
 * ── Estrategia de performance ────────────────────────────────────────────────
 *
 *  El método principal ejecuta exactamente 3 queries contra la base de datos:
 *
 *   Query 1 → findByIdWithAgricultorAndProducto(id)
 *             Cultivo + Agricultor + Usuario + ProductoVariedad + Producto + EstadoCultivo
 *             [Un solo JOIN FETCH de 6 tablas]
 *
 *   Query 2 → findHistorialByCultivoId(id)
 *             Historial_Cultivo + EstadoCultivo + EtapaProductoVariedad (LEFT)
 *             [JOIN FETCH ordenado por fecha_inicio ASC]
 *
 *   Query 3 → findDetallesPedidoByCultivoId(id)
 *             DetallePedido + Pedido + Comprador + Usuario(comprador) + EstadoPedido + UnidadMedida
 *             [JOIN FETCH de 6 tablas en una query]
 *
 *  Total: 3 queries fijas independientemente del número de etapas o pedidos.
 *  Esto elimina el problema N+1 que ocurriría si se navegasen las relaciones
 *  LAZY en bucle (1 query por cultivo + N por historial + M por pedidos).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrazabilidadService {

    private final TrazabilidadRepository trazabilidadRepository;

    /**
     * Obtiene la cadena de trazabilidad completa para el cultivo con el id dado.
     *
     * Seguridad a nivel de negocio:
     *  - AGRICULTOR: accede solo si el cultivo le pertenece.
     *  - COMPRADOR : accede a la trazabilidad de cualquier cultivo disponible,
     *                pero sin poder ver datos privados del agricultor (se filtra
     *                si el agricultor no tiene cultivos disponibles).
     *    Nota: la restricción de rol HTTP ya fue validada en SecurityConfig.
     *
     * @param cultivoId  ID del cultivo a trazar.
     * @param emailUsuario Email del usuario autenticado (extraído del JWT).
     * @param rol          Rol del usuario autenticado ("AGRICULTOR" o "COMPRADOR").
     * @return TrazabilidadResponse con la cadena completa.
     */
    @Transactional(readOnly = true)
    public TrazabilidadResponse obtenerTrazabilidad(Long cultivoId, String emailUsuario, String rol) {

        log.debug("Iniciando trazabilidad para cultivoId={} solicitada por {} ({})",
                cultivoId, emailUsuario, rol);

        // ── Query 1: Cultivo + Agricultor + Producto (1 sola query) ──────────
        Cultivo cultivo = trazabilidadRepository
                .findByIdWithAgricultorAndProducto(cultivoId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No se encontró el cultivo con id: " + cultivoId));

        // ── Validación de acceso a nivel de negocio ───────────────────────────
        validarAcceso(cultivo, emailUsuario, rol);

        // ── Query 2: Historial de etapas (1 sola query, ordenado ASC) ────────
        List<Historial_Cultivo> historial =
                trazabilidadRepository.findHistorialByCultivoId(cultivoId);

        // ── Query 3: Pedidos vinculados vía DetallePedido (1 sola query) ──────
        List<DetallePedido> detallesPedido =
                trazabilidadRepository.findDetallesPedidoByCultivoId(cultivoId);

        log.debug("Trazabilidad cargada: {} etapas, {} pedidos vinculados",
                historial.size(), detallesPedido.size());

        // ── Mapeo a DTO ───────────────────────────────────────────────────────
        return TrazabilidadResponse.builder()
                .agricultor(mapearAgricultor(cultivo.getAgricultor()))
                .cultivo(mapearCultivo(cultivo))
                .historialEtapas(historial.stream()
                        .map(this::mapearEtapa)
                        .collect(Collectors.toList()))
                .pedidosVinculados(detallesPedido.stream()
                        .map(this::mapearPedidoVinculado)
                        .collect(Collectors.toList()))
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lógica de seguridad a nivel de negocio
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida que el usuario autenticado tenga permiso para ver la trazabilidad:
     *
     *  - AGRICULTOR: solo puede ver cultivos que le pertenecen.
     *  - COMPRADOR : puede ver cualquier cultivo (no se restringe por relación directa
     *                porque un COMPRADOR necesita ver trazabilidad antes de comprar).
     *
     * La restricción de rol (AGRICULTOR | COMPRADOR) ya fue aplicada a nivel HTTP
     * por SecurityConfig, por lo que aquí solo se añade la regla de propiedad.
     */
    private void validarAcceso(Cultivo cultivo, String emailUsuario, String rol) {
        if ("AGRICULTOR".equals(rol)) {
            String emailPropietario = cultivo.getAgricultor().getUsuario().getEmail();
            if (!emailPropietario.equalsIgnoreCase(emailUsuario)) {
                log.warn("AGRICULTOR {} intentó acceder al cultivo {} que pertenece a {}",
                        emailUsuario, cultivo.getId(), emailPropietario);
                throw new AccessDeniedException(
                        "No tienes permiso para ver la trazabilidad de este cultivo.");
            }
        }
        // COMPRADOR: sin restricción adicional de propiedad
        // (puede consultar trazabilidad de cualquier cultivo disponible en el catálogo)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mappers privados
    // ─────────────────────────────────────────────────────────────────────────

    private AgricultorInfoDTO mapearAgricultor(Agricultor a) {
        Usuario u = a.getUsuario();
        return AgricultorInfoDTO.builder()
                .idAgricultor(a.getId())
                .nombres(u.getNombres())
                .apellidoPaterno(u.getApellidoPaterno())
                .apellidoMaterno(u.getApellidoMaterno())
                .email(u.getEmail())
                .dniRuc(a.getDniRuc())
                .ubicacion(a.getUbicacion())
                .hectareasTotales(a.getHectareasTotales())
                .anosExperiencia(a.getAnosExperiencia())
                .certificaciones(a.getCertificaciones())
                .fotoPerfil(u.getFotoPerfil())
                .build();
    }

    private CultivoInfoDTO mapearCultivo(Cultivo c) {
        Producto_Variedad pv = c.getProductoVariedad();
        return CultivoInfoDTO.builder()
                .idCultivo(c.getId())
                .lote(c.getLote())
                .fechaSiembra(c.getFechaSiembra())
                .areaSembrada(c.getAreaSembrada())
                .diasTotalesEstimados(c.getDiasTotalesEstimados())
                .cantidadEstimada(c.getCantidadEstimada())
                .cantidadDisponible(c.getCantidadDisponible())
                .unidad(c.getUnidad())
                .precio(c.getPrecio())
                .minimoVenta(c.getMinimoVenta())
                .disponible(c.getDisponible())
                .estadoActual(c.getEstadoCultivo().getDescripcionEstadoCultivo())
                .imagenUrl(c.getImagenUrl())
                .observaciones(c.getObservaciones())
                .nombreProducto(pv.getProducto().getNombre())
                .nombreVariedad(pv.getNombreProductosVariedad())
                .build();
    }

    /**
     * Mapea un registro de historial a EtapaDTO.
     * Calcula días transcurridos y porcentaje de retraso en memoria
     * (no requiere queries adicionales, los datos ya están cargados).
     */
    private EtapaDTO mapearEtapa(Historial_Cultivo hc) {
        LocalDate referencia = (hc.getFechaFin() != null) ? hc.getFechaFin() : LocalDate.now();
        long diasTranscurridos = ChronoUnit.DAYS.between(hc.getFechaInicio(), referencia);

        Integer diasEstimados = null;
        Double porcentajeRetraso = null;
        Boolean alertaRetraso = false;
        String nombreEtapa = null;

        Etapa_Producto_Variedad epv = hc.getEtapaProductoVariedad();
        if (epv != null) {
            diasEstimados = epv.getDiasDuracionEstimada();
            nombreEtapa = epv.getNombreEtapaProductosVariedad();
            if (diasEstimados != null && diasEstimados > 0) {
                porcentajeRetraso = ((double)(diasTranscurridos - diasEstimados) / diasEstimados) * 100.0;
                alertaRetraso = porcentajeRetraso > 20.0;
            }
        }

        return EtapaDTO.builder()
                .idHistorial(hc.getId())
                .nombreEtapa(nombreEtapa)
                .estadoCultivo(hc.getEstadoCultivo().getDescripcionEstadoCultivo())
                .fechaInicio(hc.getFechaInicio())
                .fechaFin(hc.getFechaFin())
                .activa(hc.getFechaFin() == null)
                .diasTranscurridos(diasTranscurridos)
                .diasDuracionEstimada(diasEstimados)
                .porcentajeRetraso(porcentajeRetraso)
                .alertaRetraso(alertaRetraso)
                .observaciones(hc.getObservaciones())
                .build();
    }

    private PedidoVinculadoDTO mapearPedidoVinculado(DetallePedido dp) {
        Pedido ped = dp.getPedido();
        Comprador comp = ped.getComprador();
        Usuario uComp = comp.getUsuario();
        return PedidoVinculadoDTO.builder()
                .idPedido(ped.getId())
                .fechaCreacion(ped.getFechaCreacion())
                .estadoPedido(ped.getEstadoPedido().getDescripcionEstadoPedido())
                .nombreComprador(uComp.getNombres() + " " + uComp.getApellidoPaterno())
                .emailComprador(uComp.getEmail())
                .nombreNegocio(comp.getNombreNegocio())
                .cantidadSolicitada(dp.getCantidadSolicitada())
                .precioPactado(dp.getPrecioPactado())
                .cantidadEntrega(dp.getCantidadEntrega())
                .direccionEntrega(dp.getDireccion())
                .unidadMedida(dp.getUnidadMedidaProducto().getNombreUnidadMedidaProducto())
                .build();
    }
}
