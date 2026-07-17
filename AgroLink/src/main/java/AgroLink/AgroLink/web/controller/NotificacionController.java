package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.NotificacionDTO;
import AgroLink.AgroLink.domain.service.NotificacionService;
import AgroLink.AgroLink.persistance.entity.Rol;
import AgroLink.AgroLink.persistance.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> obtener(@AuthenticationPrincipal Usuario usuario) {
        List<NotificacionDTO> resultado = usuario.getRol() == Rol.AGRICULTOR
                ? notificacionService.obtenerParaAgricultor(usuario.getEmail())
                : notificacionService.obtenerParaComprador(usuario.getEmail());

        return ResponseEntity.ok(resultado);
    }
}