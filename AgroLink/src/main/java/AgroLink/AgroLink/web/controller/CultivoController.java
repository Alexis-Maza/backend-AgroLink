package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.*;
import AgroLink.AgroLink.domain.service.CultivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cultivos")
@RequiredArgsConstructor
public class CultivoController {

    private final CultivoService cultivoService;

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

    @PostMapping("/recalcular-estados")
    public ResponseEntity<String> recalcularEstadosAgricultor(
            @AuthenticationPrincipal UserDetails userDetails) {
        cultivoService.actualizarEstadosDelAgricultor(userDetails.getUsername());
        return ResponseEntity.ok("Estados recalculados con éxito.");
    }

    @PostMapping("/admin/recalcular-todos")
    public ResponseEntity<String> recalcularEstadosGlobales() {
        cultivoService.actualizarEstadosDeCultivos();
        return ResponseEntity.ok("Estados globales actualizados con éxito.");
    }

    @PostMapping("/{id}/merma")
    public ResponseEntity<CultivoResponse> registrarMerma(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody MermaRequest request) {
        return ResponseEntity.ok(
                cultivoService.registrarMerma(id, userDetails.getUsername(), request)
        );
    }
}