package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.EstadisticasAdminDTO;
import AgroLink.AgroLink.persistance.entity.Rol;
import AgroLink.AgroLink.persistance.entity.Usuario;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import AgroLink.AgroLink.domain.repository.projection.UsuarioAdminView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;

    // ─── AGRICULTORES ─────────────────────────────────────────────

    public List<UsuarioAdminView> listarAgricultores() {
        return usuarioRepository.findByRol(Rol.AGRICULTOR);
    }

    public EstadisticasAdminDTO estadisticasAgricultores() {
        long total = usuarioRepository.countByRol(Rol.AGRICULTOR);
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        long hoy = usuarioRepository.countByRolAndFechaRegistroAfter(Rol.AGRICULTOR, inicioDia);
        return new EstadisticasAdminDTO(total, hoy);
    }

    // ─── COMPRADORES ──────────────────────────────────────────────

    public List<UsuarioAdminView> listarCompradores() {
        return usuarioRepository.findByRol(Rol.COMPRADOR);
    }

    public EstadisticasAdminDTO estadisticasCompradores() {
        long total = usuarioRepository.countByRol(Rol.COMPRADOR);
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        long hoy = usuarioRepository.countByRolAndFechaRegistroAfter(Rol.COMPRADOR, inicioDia);
        return new EstadisticasAdminDTO(total, hoy);
    }

    // ─── ACCIONES ─────────────────────────────────────────────────

    public void toggleEstado(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + idUsuario));
        usuario.setVerificado(!Boolean.TRUE.equals(usuario.getVerificado()));
        usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new RuntimeException("Usuario no encontrado: " + idUsuario);
        }
        usuarioRepository.deleteById(idUsuario);
    }
}