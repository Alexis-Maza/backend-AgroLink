package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.CambiarPasswordRequest;
import AgroLink.AgroLink.domain.dto.DatosPersonalesRequest;
import AgroLink.AgroLink.domain.dto.PerfilAgricolaRequest;
import AgroLink.AgroLink.domain.dto.PerfilAgricultorResponse;
import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Usuario;
import AgroLink.AgroLink.domain.repository.AgricultorRepository;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgricultorService {

    private final UsuarioRepository usuarioRepository;
    private final AgricultorRepository agricultorRepository;
    private final PasswordEncoder passwordEncoder;

    // Sección 1 — Datos Personales
    public void actualizarDatosPersonales(String email, DatosPersonalesRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setNombres(request.getNombres());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setEdad(request.getEdad());
        usuario.setFotoPerfil(request.getFotoPerfil());

        usuarioRepository.save(usuario);
    }

    // Sección 2 — Cambiar Password
    public void cambiarPassword(String email, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Verificar que nueva password y confirmar coincidan
        if (!request.getNuevaPassword().equals(request.getConfirmarPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }

        usuario.setPassword(passwordEncoder.encode(request.getNuevaPassword()));
        usuarioRepository.save(usuario);
    }

    // Sección 3 — Perfil Agrícola
    public void actualizarPerfilAgricola(String email, PerfilAgricolaRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Buscar si ya tiene perfil agrícola o crear uno nuevo
        Agricultor agricultor = agricultorRepository.findByUsuario(usuario)
                .orElse(new Agricultor());

        agricultor.setDescripcion(request.getDescripcion());
        agricultor.setDniRuc(request.getDniRuc());
        agricultor.setUbicacion(request.getUbicacion());
        agricultor.setHectareasTotales(request.getHectareasTotales());
        agricultor.setAnosExperiencia(request.getAnosExperiencia());
        agricultor.setCertificaciones(request.getCertificaciones());
        agricultor.setUsuario(usuario);

        agricultorRepository.save(agricultor);
    }

    public PerfilAgricultorResponse obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Agricultor agricultor = agricultorRepository.findByUsuario(usuario)
                .orElse(new Agricultor());

        return new PerfilAgricultorResponse(
                usuario.getNombres(),
                usuario.getApellidoPaterno(),
                usuario.getApellidoMaterno(),
                usuario.getEdad(),
                usuario.getEmail(),
                usuario.getFotoPerfil(),
                agricultor.getDescripcion(),
                agricultor.getDniRuc(),
                agricultor.getUbicacion(),
                agricultor.getHectareasTotales() != null ?
                        agricultor.getHectareasTotales().doubleValue() : null,
                agricultor.getAnosExperiencia(),
                agricultor.getCertificaciones()
        );
    }
}
