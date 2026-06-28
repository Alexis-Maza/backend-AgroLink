package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.ForgotPasswordRequest;
import AgroLink.AgroLink.domain.dto.ResetPasswordRequest;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import AgroLink.AgroLink.persistance.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public void forgotPassword(ForgotPasswordRequest request) {
            Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Email no encontrado"));

            String token = String.valueOf((int)(Math.random() * 900000) + 100000);
            usuario.setResetToken(token);
            usuario.setResetTokenExpiration(LocalDateTime.now().plusMinutes(5));
            usuarioRepository.save(usuario);

            emailService.sendPasswordResetEmail(usuario.getEmail(), token);

    }

    public void resetPassword(ResetPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        // Verificar que no haya expirado
        if (usuario.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado");
        }

        // Actualizar contraseña
        usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Limpiar token
        usuario.setResetToken(null);
        usuario.setResetTokenExpiration(null);
        usuarioRepository.save(usuario);
    }
}