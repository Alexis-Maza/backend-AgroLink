package AgroLink.AgroLink.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class CultivoStateScheduler {

    private final CultivoService cultivoService;

    /**
     * Tarea programada que evalúa diariamente la actualización de estados de los cultivos activos.
     * Se ejecuta todas las noches a las 2:00 AM (hora del servidor).
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void verificarYActualizarEstados() {
        System.out.println("[CultivoStateScheduler] Iniciando actualización automática de estados: " + LocalDate.now());
        try {
            cultivoService.actualizarEstadosDeCultivos();
            System.out.println("[CultivoStateScheduler] Actualización automática completada con éxito.");
        } catch (Exception e) {
            System.err.println("[CultivoStateScheduler] Error durante la actualización automática: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
