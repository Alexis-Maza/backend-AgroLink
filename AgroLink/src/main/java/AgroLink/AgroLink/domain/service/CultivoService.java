package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.*;
import AgroLink.AgroLink.domain.repository.AgricultorRepository;
import AgroLink.AgroLink.domain.repository.CultivoRepository;
import AgroLink.AgroLink.domain.repository.DetallePedidoRepository;
import AgroLink.AgroLink.domain.repository.EtapaProductoVariedadRepository;
import AgroLink.AgroLink.domain.repository.HistorialCultivoRepository;
import AgroLink.AgroLink.domain.repository.EstadoCultivoRepository;
import AgroLink.AgroLink.persistance.entity.*;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RF-A2-02: Servicio para el control de cultivos del agricultor.
 * Gestiona el CRUD de cultivos y el registro del historial de etapas.
 */
@Service
@RequiredArgsConstructor
public class CultivoService {

    private final CultivoRepository cultivoRepository;
    private final HistorialCultivoRepository historialCultivoRepository;
    private final EtapaProductoVariedadRepository etapaProductoVariedadRepository;
    private final AgricultorRepository agricultorRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoCultivoRepository estadoCultivoRepository;
    private final PedidoCancelacionService pedidoCancelacionService;
    private final DetallePedidoRepository detallePedidoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Sección 1: CRUD de Cultivos ────────────────────────────────────────

    @Transactional
    public CultivoResponse registrarCultivo(String email, CultivoRequest request) {
        Agricultor agricultor = obtenerAgricultorPorEmail(email);

        // Calcular estado por porcentaje de días transcurridos
        String estadoNombre = calcularEstadoPorPorcentaje(request.getFechaSiembra(), request.getDiasTotalesEstimados());
        Estado_Cultivo estado = obtenerEstadoPorNombre(estadoNombre);

        Cultivo cultivo = new Cultivo();
        cultivo.setFechaSiembra(request.getFechaSiembra());
        cultivo.setAreaSembrada(request.getAreaSembrada());
        cultivo.setDiasTotalesEstimados(request.getDiasTotalesEstimados());
        cultivo.setAgricultor(agricultor);
        cultivo.setEstadoCultivo(estado);
        cultivo.setDisponible(estadoNombre.equals("Listo para cosechar"));
        cultivo.setLote(request.getLote());
        cultivo.setPrecio(request.getPrecio());
        cultivo.setMinimoVenta(request.getMinimoVenta());
        cultivo.setCantidadEstimada(request.getCantidadEstimada());
        cultivo.setCantidadDisponible(request.getCantidadEstimada());
        cultivo.setUnidad(request.getUnidad());
        cultivo.setImagenUrl(request.getImagenUrl());

        if (request.getIdProductoVariedad() != null) {
            Producto_Variedad pv = new Producto_Variedad();
            pv.setId(request.getIdProductoVariedad());
            cultivo.setProductoVariedad(pv);
        }

        Cultivo cultivoGuardado = cultivoRepository.save(cultivo);

        // Registrar la etapa inicial en Historial_Cultivo
        Historial_Cultivo historial = new Historial_Cultivo();
        historial.setFechaInicio(request.getFechaSiembra());
        historial.setFechaFin(null);
        historial.setCultivo(cultivoGuardado);
        historial.setEstadoCultivo(estado);
        historialCultivoRepository.save(historial);

        return mapearCultivoAResponse(cultivoGuardado);
    }

    public List<CultivoResponse> listarCultivosPorAgricultor(String email) {
        Agricultor agricultor = obtenerAgricultorPorEmail(email);
        return cultivoRepository.findByAgricultorOrderByFechaSiembraDesc(agricultor)
                .stream()
                .map(this::mapearCultivoAResponse)
                .collect(Collectors.toList());
    }

    public CultivoResponse obtenerCultivoPorId(Long idCultivo, String email) {
        Cultivo cultivo = cultivoRepository.findById(idCultivo)
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        validarPropietario(cultivo, email);
        return mapearCultivoAResponse(cultivo);
    }

    @Transactional
    public CultivoResponse actualizarCultivo(Long idCultivo, String email, CultivoRequest request) {
        Cultivo cultivo = cultivoRepository.findById(idCultivo)
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        validarPropietario(cultivo, email);

        cultivo.setFechaSiembra(request.getFechaSiembra());
        cultivo.setAreaSembrada(request.getAreaSembrada());
        if (request.getLote() != null) cultivo.setLote(request.getLote());
        if (request.getPrecio() != null) cultivo.setPrecio(request.getPrecio());
        if (request.getMinimoVenta() != null) cultivo.setMinimoVenta(request.getMinimoVenta());
        if (request.getCantidadEstimada() != null) cultivo.setCantidadEstimada(request.getCantidadEstimada());
        if (request.getUnidad() != null) cultivo.setUnidad(request.getUnidad());
        if (request.getImagenUrl() != null) cultivo.setImagenUrl(request.getImagenUrl());
        if (request.getDiasTotalesEstimados() != null) {
            cultivo.setDiasTotalesEstimados(request.getDiasTotalesEstimados());
        }

        if (request.getIdProductoVariedad() != null) {
            Producto_Variedad pv = new Producto_Variedad();
            pv.setId(request.getIdProductoVariedad());
            cultivo.setProductoVariedad(pv);
        }

        // Recalcular estado por si cambiaron días o fecha
        String estadoNuevoNombre = calcularEstadoPorPorcentaje(cultivo.getFechaSiembra(), cultivo.getDiasTotalesEstimados());
        Estado_Cultivo estadoAnterior = cultivo.getEstadoCultivo();

        if (estadoAnterior == null || !estadoNuevoNombre.equals(estadoAnterior.getDescripcionEstadoCultivo())) {
            Estado_Cultivo nuevoEstado = obtenerEstadoPorNombre(estadoNuevoNombre);
            cultivo.setEstadoCultivo(nuevoEstado);

            // Cerrar etapa activa
            historialCultivoRepository.findByCultivoAndFechaFinIsNull(cultivo)
                    .ifPresent(etapaAnterior -> {
                        etapaAnterior.setFechaFin(LocalDate.now());
                        historialCultivoRepository.save(etapaAnterior);
                    });

            // Abrir nueva etapa
            Historial_Cultivo nuevaEtapa = new Historial_Cultivo();
            nuevaEtapa.setFechaInicio(LocalDate.now());
            nuevaEtapa.setFechaFin(null);
            nuevaEtapa.setCultivo(cultivo);
            nuevaEtapa.setEstadoCultivo(nuevoEstado);
            historialCultivoRepository.save(nuevaEtapa);
        }

        cultivo.setDisponible(estadoNuevoNombre.equals("Listo para cosechar"));

        return mapearCultivoAResponse(cultivoRepository.save(cultivo));
    }

    @Transactional
    public void eliminarCultivo(Long idCultivo, String email) {
        Cultivo cultivo = cultivoRepository.findById(idCultivo)
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        validarPropietario(cultivo, email);

        // Primero eliminar el historial asociado
        historialCultivoRepository.deleteAllByCultivo(cultivo);

        // Luego eliminar el cultivo
        cultivoRepository.delete(cultivo);
    }

    // ── Sección 2: Historial de Etapas ──────────────────────────────────────

    @Transactional
    public HistorialCultivoResponse registrarEtapa(Long idCultivo, String email,
                                                    HistorialCultivoRequest request) {
        Cultivo cultivo = cultivoRepository.findById(idCultivo)
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        validarPropietario(cultivo, email);

        // Cerrar la etapa activa anterior si existe
        historialCultivoRepository.findByCultivoAndFechaFinIsNull(cultivo)
                .ifPresent(etapaAnterior -> {
                    etapaAnterior.setFechaFin(request.getFechaInicio().minusDays(1));
                    historialCultivoRepository.save(etapaAnterior);
                });

        // Crear nueva etapa manual
        Historial_Cultivo nuevaEtapa = new Historial_Cultivo();
        nuevaEtapa.setFechaInicio(request.getFechaInicio());
        nuevaEtapa.setCultivo(cultivo);
        
        // Conservamos el estado del cultivo actual
        nuevaEtapa.setEstadoCultivo(cultivo.getEstadoCultivo());

        if (request.getIdEtapaProductoVariedad() != null) {
            Etapa_Producto_Variedad referencia = etapaProductoVariedadRepository
                    .findById(request.getIdEtapaProductoVariedad())
                    .orElseThrow(() -> new RuntimeException("Etapa de referencia no encontrada"));
            nuevaEtapa.setEtapaProductoVariedad(referencia);
        }

        Historial_Cultivo etapaGuardada = historialCultivoRepository.save(nuevaEtapa);
        return mapearHistorialAResponse(etapaGuardada);
    }

    public List<HistorialCultivoResponse> obtenerHistorialDeCultivo(Long idCultivo, String email) {
        Cultivo cultivo = cultivoRepository.findById(idCultivo)
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        validarPropietario(cultivo, email);

        return historialCultivoRepository.findByCultivoOrderByFechaInicioAsc(cultivo)
                .stream()
                .map(this::mapearHistorialAResponse)
                .collect(Collectors.toList());
    }

    // ── Sección 3: Métodos de Lógica de Negocio y Estados ────────────────────

    public String calcularEstadoPorPorcentaje(LocalDate fechaSiembra, Integer diasTotalesEstimados) {
        if (diasTotalesEstimados == null || diasTotalesEstimados <= 0) {
            return "Recién cultivado";
        }
        long diasTranscurridos = ChronoUnit.DAYS.between(fechaSiembra, LocalDate.now());
        if (diasTranscurridos < 0) {
            diasTranscurridos = 0;
        }
        double porcentaje = ((double) diasTranscurridos / diasTotalesEstimados) * 100.0;

        if (porcentaje <= 20.0) {
            return "Recién cultivado";
        } else if (porcentaje <= 80.0) {
            return "En crecimiento";
        } else {
            return "Listo para cosechar";
        }
    }

    @Transactional
    public void actualizarEstadosDeCultivos() {
        List<Cultivo> cultivosActivos = cultivoRepository.findByEstadoCultivoDescripcionEstadoCultivoIn(
                List.of("Recién cultivado", "En crecimiento")
        );

        for (Cultivo cultivo : cultivosActivos) {
            String nuevoEstadoNombre = calcularEstadoPorPorcentaje(cultivo.getFechaSiembra(), cultivo.getDiasTotalesEstimados());
            String estadoActualNombre = cultivo.getEstadoCultivo().getDescripcionEstadoCultivo();

            if (!nuevoEstadoNombre.equals(estadoActualNombre)) {
                Estado_Cultivo nuevoEstado = obtenerEstadoPorNombre(nuevoEstadoNombre);
                cultivo.setEstadoCultivo(nuevoEstado);

                historialCultivoRepository.findByCultivoAndFechaFinIsNull(cultivo)
                        .ifPresent(etapaAnterior -> {
                            etapaAnterior.setFechaFin(LocalDate.now());
                            historialCultivoRepository.save(etapaAnterior);
                        });

                Historial_Cultivo nuevaEtapa = new Historial_Cultivo();
                nuevaEtapa.setFechaInicio(LocalDate.now());
                nuevaEtapa.setFechaFin(null);
                nuevaEtapa.setCultivo(cultivo);
                nuevaEtapa.setEstadoCultivo(nuevoEstado);
                historialCultivoRepository.save(nuevaEtapa);

                if (nuevoEstadoNombre.equals("Listo para cosechar")) {
                    // --- NUEVO: notificación al agricultor ---
                    NotificacionDTO notif = new NotificacionDTO(
                            "historial-cultivo-" + nuevaEtapa.getId(),
                            "CULTIVO_LISTO",
                            "El cultivo del lote '" + cultivo.getLote() + "' superó su fecha estimada y ya está listo para cosechar",
                            LocalDateTime.now(),
                            cultivo.getId()
                    );
                    messagingTemplate.convertAndSend("/topic/agricultor/" + cultivo.getAgricultor().getId(), notif);
                }

                cultivoRepository.save(cultivo);
            }
        }
    }

    @Transactional
    public void actualizarEstadosDelAgricultor(String email) {
        Agricultor agricultor = obtenerAgricultorPorEmail(email);
        List<Cultivo> cultivos = cultivoRepository.findByAgricultor(agricultor);

        for (Cultivo cultivo : cultivos) {
            String nuevoEstadoNombre = calcularEstadoPorPorcentaje(cultivo.getFechaSiembra(), cultivo.getDiasTotalesEstimados());
            Estado_Cultivo estadoActual = cultivo.getEstadoCultivo();

            if (estadoActual == null || !nuevoEstadoNombre.equals(estadoActual.getDescripcionEstadoCultivo())) {
                Estado_Cultivo nuevoEstado = obtenerEstadoPorNombre(nuevoEstadoNombre);
                cultivo.setEstadoCultivo(nuevoEstado);

                historialCultivoRepository.findByCultivoAndFechaFinIsNull(cultivo)
                        .ifPresent(etapaAnterior -> {
                            etapaAnterior.setFechaFin(LocalDate.now());
                            historialCultivoRepository.save(etapaAnterior);
                        });

                Historial_Cultivo nuevaEtapa = new Historial_Cultivo();
                nuevaEtapa.setFechaInicio(LocalDate.now());
                nuevaEtapa.setFechaFin(null);
                nuevaEtapa.setCultivo(cultivo);
                nuevaEtapa.setEstadoCultivo(nuevoEstado);
                historialCultivoRepository.save(nuevaEtapa);

                if (nuevoEstadoNombre.equals("Listo para cosechar")) {
                    cultivo.setDisponible(true);

                    // --- NUEVO: notificación al agricultor ---
                    NotificacionDTO notif = new NotificacionDTO(
                            "historial-cultivo-" + nuevaEtapa.getId(),
                            "CULTIVO_LISTO",
                            "El cultivo del lote '" + cultivo.getLote() + "' superó su fecha estimada y ya está listo para cosechar",
                            LocalDateTime.now(),
                            cultivo.getId()
                    );
                    messagingTemplate.convertAndSend("/topic/agricultor/" + cultivo.getAgricultor().getId(), notif);
                }

                cultivoRepository.save(cultivo);
            }
        }
    }

    private Estado_Cultivo obtenerEstadoPorNombre(String nombre) {
        return estadoCultivoRepository.findByDescripcionEstadoCultivo(nombre)
                .orElseGet(() -> {
                    Estado_Cultivo ne = new Estado_Cultivo();
                    ne.setDescripcionEstadoCultivo(nombre);
                    return estadoCultivoRepository.save(ne);
                });
    }

    // ── Sección 4: Métodos auxiliares ───────────────────────────────────────

    private Agricultor obtenerAgricultorPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return agricultorRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Perfil de agricultor no encontrado"));
    }

    private void validarPropietario(Cultivo cultivo, String email) {
        if (!cultivo.getAgricultor().getUsuario().getEmail().equals(email)) {
            throw new RuntimeException("No tienes permiso para acceder a este cultivo");
        }
    }

    /**
     * RF-AF-07: stock físico restante de un cultivo = lo no vendido (cantidadDisponible)
     * más lo ya reservado en pedidos activos (Pendiente/En preparación) todavía no despachados.
     */
    private BigDecimal calcularStockTotalRestante(Cultivo cultivo) {
        BigDecimal reservadoEnPedidosActivos = detallePedidoRepository
                .findByCultivoAndPedido_EstadoPedido_DescripcionEstadoPedidoIn(cultivo, PedidoCancelacionService.ESTADOS_ACTIVOS)
                .stream()
                .map(DetallePedido::getCantidadSolicitada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return cultivo.getCantidadDisponible().add(reservadoEnPedidosActivos);
    }

    private CultivoResponse mapearCultivoAResponse(Cultivo cultivo) {
        Boolean alertaRetraso = historialCultivoRepository
                .findByCultivoAndFechaFinIsNull(cultivo)
                .map(this::calcularSiHayRetraso)
                .orElse(false);

        Producto_Variedad pv = cultivo.getProductoVariedad();
        String nombreProducto = null;
        String nombreVariedad = null;
        Long idVariedad = null;

        if (pv != null) {
            idVariedad = pv.getId();
            nombreVariedad = pv.getNombreProductosVariedad();
            // Verificar que el Producto no sea null antes de acceder
            if (pv.getProducto() != null) {
                nombreProducto = pv.getProducto().getNombre();
            }
        }

        BigDecimal stockTotalRestante = calcularStockTotalRestante(cultivo);

        return new CultivoResponse(
                cultivo.getId(),
                cultivo.getFechaSiembra(),
                cultivo.getAreaSembrada(),
                cultivo.getEstadoCultivo() != null ? cultivo.getEstadoCultivo().getDescripcionEstadoCultivo() : null,
                alertaRetraso,
                idVariedad,
                nombreProducto,
                nombreVariedad,
                cultivo.getDiasTotalesEstimados(),
                cultivo.getDisponible(),
                cultivo.getLote(),
                cultivo.getPrecio(),
                cultivo.getMinimoVenta(),
                cultivo.getCantidadEstimada(),
                cultivo.getCantidadDisponible(),
                cultivo.getUnidad(),
                cultivo.getImagenUrl(),
                stockTotalRestante
        );
    }

    public HistorialCultivoResponse mapearHistorialAResponse(Historial_Cultivo historial) {
        LocalDate fechaReferencia = historial.getFechaFin() != null
                ? historial.getFechaFin()
                : LocalDate.now();

        long diasTranscurridos = ChronoUnit.DAYS.between(historial.getFechaInicio(), fechaReferencia);

        Integer diasDuracionEstimada = null;
        Double porcentajeRetraso = null;

        if (historial.getEtapaProductoVariedad() != null) {
            diasDuracionEstimada = historial.getEtapaProductoVariedad().getDiasDuracionEstimada();
            if (diasDuracionEstimada != null && diasDuracionEstimada > 0) {
                porcentajeRetraso = ((double)(diasTranscurridos - diasDuracionEstimada)
                        / diasDuracionEstimada) * 100.0;
            }
        }

        return new HistorialCultivoResponse(
                historial.getId(),
                historial.getFechaInicio(),
                historial.getFechaFin(),
                diasTranscurridos,
                diasDuracionEstimada,
                calcularSiHayRetraso(historial),
                porcentajeRetraso,
                historial.getEstadoCultivo() != null
                        ? historial.getEstadoCultivo().getDescripcionEstadoCultivo()
                        : null
        );
    }

    private boolean calcularSiHayRetraso(Historial_Cultivo historial) {
        if (historial.getEtapaProductoVariedad() == null) {
            return false;
        }

        Integer diasDuracionEstimada = historial.getEtapaProductoVariedad().getDiasDuracionEstimada();
        if (diasDuracionEstimada == null || diasDuracionEstimada <= 0) {
            return false;
        }

        LocalDate fechaReferencia = historial.getFechaFin() != null
                ? historial.getFechaFin()
                : LocalDate.now();

        long diasTranscurridos = ChronoUnit.DAYS.between(
                historial.getFechaInicio(),
                fechaReferencia
        );

        // Umbral: días de duración estimada + 20%
        double umbralRetraso = diasDuracionEstimada * 1.20;

        return diasTranscurridos > umbralRetraso;
    }

    // ── Sección 5: Métodos para catálogo de compradores ──────────────────────

    public List<CultivoResponse> obtenerCultivosDisponibles() {
        return cultivoRepository.findByDisponibleTrue()
                .stream()
                .map(this::mapearCultivoAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CultivoResponse registrarMerma(Long idCultivo, String email, MermaRequest request) {
        Cultivo cultivo = cultivoRepository.findById(idCultivo)
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        validarPropietario(cultivo, email);

        // El stock que una merma puede afectar incluye tanto lo no vendido (cantidadDisponible)
        // como lo ya reservado en pedidos activos (Pendiente/En preparación) de este cultivo:
        // una pérdida real (plaga, etc.) puede destruir producto ya comprometido con un comprador.
        BigDecimal stockTotalRestante = calcularStockTotalRestante(cultivo);

        if (request.getCantidadPerdida().compareTo(stockTotalRestante) > 0) {
            throw new RuntimeException(
                    "La cantidad perdida no puede ser mayor al stock total restante (disponible + reservado en pedidos activos)");
        }

        // Restar la merma del stock disponible
        BigDecimal nuevoStock = cultivo.getCantidadDisponible()
                .subtract(request.getCantidadPerdida());
        cultivo.setCantidadDisponible(nuevoStock);

        Cultivo cultivoGuardado = cultivoRepository.save(cultivo);

        // RF-AF-07: cancelar pedidos activos que ya no tengan stock suficiente tras la merma.
        // Se evalúa con el stock (posiblemente negativo) recién guardado: ese valor "de más"
        // representa lo que la merma se comió del stock reservado en pedidos activos.
        String motivo = "Cancelado por merma"
                + (request.getCausa() != null && !request.getCausa().isBlank() ? ": " + request.getCausa() : "");
        pedidoCancelacionService.evaluarCancelacionesPorCultivo(cultivoGuardado, motivo);

        // Una vez canceladas las órdenes que ya no caben, no tiene sentido mostrarle al
        // agricultor un stock disponible negativo: se normaliza a 0 para la vista.
        if (cultivoGuardado.getCantidadDisponible().compareTo(BigDecimal.ZERO) < 0) {
            cultivoGuardado.setCantidadDisponible(BigDecimal.ZERO);
            cultivoGuardado = cultivoRepository.save(cultivoGuardado);
        }

        return mapearCultivoAResponse(cultivoGuardado);
    }

    @Transactional
    public CultivoResponse confirmarCosecha(Long idCultivo, String email, BigDecimal volumenCosechado) {
        Cultivo cultivo = cultivoRepository.findById(idCultivo)
                .orElseThrow(() -> new RuntimeException("Cultivo no encontrado"));
        validarPropietario(cultivo, email);

        if (!cultivo.getEstadoCultivo().getDescripcionEstadoCultivo().equals("Listo para cosechar")) {
            throw new RuntimeException("Solo se puede confirmar la cosecha de un cultivo en estado 'Listo para cosechar'");
        }

        if (volumenCosechado == null || volumenCosechado.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El volumen cosechado debe ser mayor a cero");
        }

        // Calcular rendimiento: real vs estimado
        BigDecimal rendimientoPorcentaje = cultivo.getCantidadEstimada() != null
                && cultivo.getCantidadEstimada().compareTo(BigDecimal.ZERO) > 0
                ? volumenCosechado.divide(cultivo.getCantidadEstimada(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : null;

        // Cerrar la etapa activa
        historialCultivoRepository.findByCultivoAndFechaFinIsNull(cultivo)
                .ifPresent(etapaActiva -> {
                    etapaActiva.setFechaFin(LocalDate.now());
                    historialCultivoRepository.save(etapaActiva);
                });

        // Nuevo estado "Cosechado"
        Estado_Cultivo estadoCosechado = obtenerEstadoPorNombre("Cosechado");
        cultivo.setEstadoCultivo(estadoCosechado);

        // Registrar la nueva etapa (cerrada de inmediato, ya que "Cosechado" es un punto final, no un rango activo)
        Historial_Cultivo etapaCosecha = new Historial_Cultivo();
        etapaCosecha.setFechaInicio(LocalDate.now());
        etapaCosecha.setFechaFin(LocalDate.now());
        etapaCosecha.setCultivo(cultivo);
        etapaCosecha.setEstadoCultivo(estadoCosechado);
        etapaCosecha.setObservaciones(
                "Volumen cosechado: " + volumenCosechado + " " + cultivo.getUnidad()
                        + (rendimientoPorcentaje != null ? " (Rendimiento: " + rendimientoPorcentaje + "% del estimado)" : "")
        );
        historialCultivoRepository.save(etapaCosecha);

        // Ahora sí: volumen real disponible para venta
        cultivo.setCantidadDisponible(volumenCosechado);
        cultivo.setDisponible(true);
        Cultivo guardado = cultivoRepository.save(cultivo);

        return mapearCultivoAResponse(guardado);
    }
}
