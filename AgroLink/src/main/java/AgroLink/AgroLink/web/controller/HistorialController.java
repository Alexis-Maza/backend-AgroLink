package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.service.AlertaRetrasoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RF-A2-05: Endpoint para forzar manualmente la verificación de alertas.
 * Útil para pruebas mientras se trabaja en modo local/en memoria.
 */
@RestController
@RequestMapping("/alertas")
@RequiredArgsConstructor
public class HistorialController {

    private final AlertaRetrasoService alertaRetrasoService;

    @PostMapping("/verificar-retrasos")
    public ResponseEntity<String> verificarRetrasosManuales() {
        String resultado = alertaRetrasoService.ejecutarVerificacionManual();
        return ResponseEntity.ok(resultado);
    }
}
