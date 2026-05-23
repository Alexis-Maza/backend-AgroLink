package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.CambiarPasswordRequest;
import AgroLink.AgroLink.domain.dto.DatosPersonalesRequest;
import AgroLink.AgroLink.domain.dto.PerfilAgricolaRequest;
import AgroLink.AgroLink.domain.service.AgricultorService;
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
}
