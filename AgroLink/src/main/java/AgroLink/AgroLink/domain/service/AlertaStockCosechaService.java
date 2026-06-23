package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.repository.CultivoRepository;
import AgroLink.AgroLink.persistance.entity.Cultivo;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertaStockCosechaService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> ESTADOS_INACTIVOS = List.of("COSECHADO", "CANCELADO");

    private final CultivoRepository cultivoRepository;
    private final EmailService emailService;

    /**
     * Se ejecuta todos los dias a las 7:00 AM.
     * Regla 1: alerta cuando cantidadDisponible <= minimoVenta.
     * Regla 2: alerta cuando faltan 5 dias o menos para la fecha estimada de cosecha.
     */
    @Scheduled(cron = "0 0 7 * * *") 
    @Transactional(readOnly = true)
    public void verificarAlertas() {
        System.out.println("[AlertaStockCosechaService] Iniciando verificacion: " + LocalDate.now());

        List<Cultivo> cultivos = cultivoRepository
                .findByEstadoCultivoDescripcionEstadoCultivoNotIn(ESTADOS_INACTIVOS);

        int alertasStock = 0;
        int alertasCosecha = 0;

        for (Cultivo cultivo : cultivos) {
            String email = cultivo.getAgricultor().getUsuario().getEmail();
            if (email == null || email.isBlank()) {
                continue;
            }

            String nombreAgricultor = cultivo.getAgricultor().getUsuario().getNombres();
            String lote = cultivo.getLote() != null ? cultivo.getLote() : "Sin lote";
            String producto = cultivo.getProductoVariedad() != null
                    ? cultivo.getProductoVariedad().getNombreProductosVariedad()
                    : "Sin producto";
            String unidad = cultivo.getUnidad() != null ? cultivo.getUnidad() : "";

            if (esStockMinimo(cultivo)) {
                try {
                    emailService.sendAlertaStockMinimo(
                            email,
                            nombreAgricultor != null ? nombreAgricultor : "Agricultor",
                            lote,
                            producto,
                            cultivo.getCantidadDisponible().toPlainString(),
                            cultivo.getMinimoVenta().toPlainString(),
                            unidad
                    );
                    alertasStock++;
                } catch (MessagingException ex) {
                    System.err.println("[AlertaStockCosechaService] Error enviando alerta de stock a "
                            + email + ": " + ex.getMessage());
                }
            }

            long diasRestantes = diasParaCosecha(cultivo);
            if (diasRestantes >= 0 && diasRestantes <= 5) {
                try {
                    LocalDate fechaCosecha = cultivo.getFechaSiembra().plusDays(cultivo.getDiasTotalesEstimados());
                    emailService.sendAlertaCosecha(
                            email,
                            nombreAgricultor != null ? nombreAgricultor : "Agricultor",
                            lote,
                            producto,
                            fechaCosecha.format(DATE_FMT),
                            diasRestantes
                    );
                    alertasCosecha++;
                } catch (MessagingException ex) {
                    System.err.println("[AlertaStockCosechaService] Error enviando alerta de cosecha a "
                            + email + ": " + ex.getMessage());
                }
            }
        }

        System.out.println("[AlertaStockCosechaService] Verificacion completada. "
                + "Stock: " + alertasStock + " | Cosecha: " + alertasCosecha
                + " | Evaluados: " + cultivos.size());
    }

    private boolean esStockMinimo(Cultivo cultivo) {
        BigDecimal disponible = cultivo.getCantidadDisponible();
        BigDecimal minimo = cultivo.getMinimoVenta();

        return disponible != null
                && minimo != null
                && disponible.compareTo(BigDecimal.ZERO) >= 0
                && disponible.compareTo(minimo) <= 0;
    }

    private long diasParaCosecha(Cultivo cultivo) {
        if (cultivo.getFechaSiembra() == null || cultivo.getDiasTotalesEstimados() == null) {
            return -1;
        }

        LocalDate fechaCosecha = cultivo.getFechaSiembra().plusDays(cultivo.getDiasTotalesEstimados());
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaCosecha);
    }
}
