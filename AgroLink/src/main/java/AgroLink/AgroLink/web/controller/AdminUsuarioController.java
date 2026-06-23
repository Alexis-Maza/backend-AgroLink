package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.EstadisticasAdminDTO;
import AgroLink.AgroLink.domain.service.AdminUsuarioService;
import AgroLink.AgroLink.domain.repository.projection.UsuarioAdminView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    // ─── AGRICULTORES ─────────────────────────────────────────────

    @GetMapping("/agricultores")
    public ResponseEntity<List<UsuarioAdminView>> listarAgricultores() {
        return ResponseEntity.ok(adminUsuarioService.listarAgricultores());
    }

    @GetMapping("/agricultores/estadisticas")
    public ResponseEntity<EstadisticasAdminDTO> estadisticasAgricultores() {
        return ResponseEntity.ok(adminUsuarioService.estadisticasAgricultores());
    }

    // ─── COMPRADORES ──────────────────────────────────────────────

    @GetMapping("/compradores")
    public ResponseEntity<List<UsuarioAdminView>> listarCompradores() {
        return ResponseEntity.ok(adminUsuarioService.listarCompradores());
    }

    @GetMapping("/compradores/estadisticas")
    public ResponseEntity<EstadisticasAdminDTO> estadisticasCompradores() {
        return ResponseEntity.ok(adminUsuarioService.estadisticasCompradores());
    }

    // ─── ACCIONES COMPARTIDAS ─────────────────────────────────────

    @PatchMapping("/usuarios/{id}/toggle-estado")
    public ResponseEntity<Void> toggleEstado(@PathVariable Long id) {
        adminUsuarioService.toggleEstado(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        adminUsuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
