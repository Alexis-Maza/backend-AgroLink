package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.CultivoRequest;
import AgroLink.AgroLink.domain.dto.CultivoResponse;
import AgroLink.AgroLink.domain.dto.HistorialCultivoRequest;
import AgroLink.AgroLink.domain.dto.HistorialCultivoResponse;
import AgroLink.AgroLink.domain.service.CultivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RF-A2-02: Endpoints para el control de cultivos del agricultor.
 * Requiere autenticación con rol AGRICULTOR.
 */
@RestController
@RequestMapping("/cultivos")
@RequiredArgsConstructor
public class CultivoController {

    private final CultivoService cultivoService;

    // ── CRUD de Cultivos ──────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<CultivoResponse> registrarCultivo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CultivoRequest request) {

        return ResponseEntity.ok(
                cultivoService.registrarCultivo(userDetails.getUsername(), request)
        );
    }

    @GetMapping
    public ResponseEntity<List<CultivoResponse>> listarCultivos(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                cultivoService.listarCultivosPorAgricultor(userDetails.getUsername())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CultivoResponse> obtenerCultivo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                cultivoService.obtenerCultivoPorId(id, userDetails.getUsername())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CultivoResponse> actualizarCultivo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CultivoRequest request) {

        return ResponseEntity.ok(
                cultivoService.actualizarCultivo(id, userDetails.getUsername(), request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarCultivo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        cultivoService.eliminarCultivo(id, userDetails.getUsername());
        return ResponseEntity.ok("Cultivo eliminado correctamente");
    }

    // ── Historial de Etapas ───────────────────────────────────────────────

    @PostMapping("/{id}/etapas")
    public ResponseEntity<HistorialCultivoResponse> registrarEtapa(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody HistorialCultivoRequest request) {

        return ResponseEntity.ok(
                cultivoService.registrarEtapa(id, userDetails.getUsername(), request)
        );
    }

    @GetMapping("/{id}/etapas")
    public ResponseEntity<List<HistorialCultivoResponse>> obtenerHistorial(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                cultivoService.obtenerHistorialDeCultivo(id, userDetails.getUsername())
        );
    }

    // ── Lógica de Negocio: Actualizaciones Manuales ───────────────────────

    /**
     * Permite al agricultor recalcular manualmente el estado de sus propios cultivos.
     */
    @PostMapping("/recalcular-estados")
    public ResponseEntity<String> recalcularEstadosAgricultor(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        cultivoService.actualizarEstadosDelAgricultor(userDetails.getUsername());
        return ResponseEntity.ok("Estados de tus cultivos recalculados y actualizados con éxito.");
    }

    /**
     * Endpoint de prueba/admin para forzar el recálculo global de todos los cultivos activos.
     */
    @PostMapping("/admin/recalcular-todos")
    public ResponseEntity<String> recalcularEstadosGlobales() {
        cultivoService.actualizarEstadosDeCultivos();
        return ResponseEntity.ok("Estados de todos los cultivos activos actualizados globalmente con éxito.");
    }
}
