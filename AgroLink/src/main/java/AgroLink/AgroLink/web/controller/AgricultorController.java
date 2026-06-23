package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.CambiarPasswordRequest;
import AgroLink.AgroLink.domain.dto.DashboardResumenResponse;
import AgroLink.AgroLink.domain.dto.DatosPersonalesRequest;
import AgroLink.AgroLink.domain.dto.PerfilAgricolaRequest;
import AgroLink.AgroLink.domain.dto.PerfilAgricultorResponse;
import AgroLink.AgroLink.domain.service.AgricultorService;
import AgroLink.AgroLink.domain.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agricultor")
@RequiredArgsConstructor
public class AgricultorController {

    private final AgricultorService agricultorService;
    private final DashboardService  dashboardService;

    @PutMapping("/datos-personales")
    public ResponseEntity<String> actualizarDatosPersonales(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DatosPersonalesRequest request) {

        agricultorService.actualizarDatosPersonales(userDetails.getUsername(), request);
        return ResponseEntity.ok("Datos personales actualizados correctamente");
    }

    @PutMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CambiarPasswordRequest request) {

        agricultorService.cambiarPassword(userDetails.getUsername(), request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @PutMapping("/perfil-agricola")
    public ResponseEntity<String> actualizarPerfilAgricola(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PerfilAgricolaRequest request) {

        agricultorService.actualizarPerfilAgricola(userDetails.getUsername(), request);
        return ResponseEntity.ok("Perfil agrícola actualizado correctamente");
    }

    @GetMapping("/perfil")
    public ResponseEntity<PerfilAgricultorResponse> obtenerPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                agricultorService.obtenerPerfil(userDetails.getUsername())
        );
    }

    /**
     * GET /agricultor/dashboard
     * Devuelve las métricas clave del panel de control del agricultor autenticado:
     * conteo de cultivos por estado, volumen disponible y ventas del mes.
     *
     * Protegido por SecurityConfig: requiere rol AGRICULTOR.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResumenResponse> obtenerDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                dashboardService.obtenerDashboard(userDetails.getUsername())
        );
    }
}
