package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.CambiarPasswordRequest;
import AgroLink.AgroLink.domain.dto.DatosPersonalesCompradorRequest;
import AgroLink.AgroLink.domain.dto.PerfilComercialRequest;
import AgroLink.AgroLink.domain.service.CompradorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comprador")
@RequiredArgsConstructor
public class CompradorController {

    private final CompradorService compradorService;

    @PutMapping("/datos-personales")
    public ResponseEntity<String> actualizarDatosPersonales(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DatosPersonalesCompradorRequest request) {

        compradorService.actualizarDatosPersonales(userDetails.getUsername(), request);
        return ResponseEntity.ok("Datos personales actualizados correctamente");
    }

    @PutMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CambiarPasswordRequest request) {

        compradorService.cambiarPassword(userDetails.getUsername(), request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }

    @PutMapping("/perfil-comercial")
    public ResponseEntity<String> actualizarPerfilComercial(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PerfilComercialRequest request) {

        compradorService.actualizarPerfilComercial(userDetails.getUsername(), request);
        return ResponseEntity.ok("Perfil comercial actualizado correctamente");
    }
}