package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.AuthRequest;
import AgroLink.AgroLink.domain.dto.AuthResponse;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import AgroLink.AgroLink.persistance.entity.Rol;
import AgroLink.AgroLink.persistance.entity.Usuario;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public String register(AuthRequest request) {
        if (request.getRol().equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("No puedes registrarte como ADMIN");
        }

        // Verificar que el email no esté registrado
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Generar código de verificación
        String codigo = String.valueOf((int)(Math.random() * 900000) + 100000);

        Usuario usuario = new Usuario();
        usuario.setNombres(request.getNombres());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.valueOf(request.getRol().toUpperCase()));
        usuario.setCodigoVerificacion(codigo);
        usuario.setVerificado(false);

        usuarioRepository.save(usuario);

        // Enviar email con código
        try {
            emailService.sendVerificationEmail(request.getEmail(), codigo);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el email de verificación");
        }

        return "Código de verificación enviado a " + request.getEmail();
    }

    public AuthResponse verifyEmail(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getCodigoVerificacion().equals(codigo)) {
            throw new RuntimeException("Código incorrecto");
        }

        // Marcar como verificado y limpiar código
        usuario.setVerificado(true);
        usuario.setCodigoVerificacion(null);
        usuarioRepository.save(usuario);

        // Ahora sí generar el token
        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token);
    }

    public AuthResponse login(AuthRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la cuenta esté verificada
        if (!usuario.getVerificado()) {
            throw new RuntimeException("Debes verificar tu correo antes de iniciar sesión");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token);
    }
}
