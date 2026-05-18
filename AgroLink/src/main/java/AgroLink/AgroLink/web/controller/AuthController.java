package AgroLink.AgroLink.web.controller;

import AgroLink.AgroLink.domain.dto.AuthRequest;
import AgroLink.AgroLink.domain.dto.AuthResponse;
import AgroLink.AgroLink.domain.dto.ForgotPasswordRequest;
import AgroLink.AgroLink.domain.dto.ResetPasswordRequest;
import AgroLink.AgroLink.domain.service.AuthService;
import AgroLink.AgroLink.domain.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return ResponseEntity.ok("Email enviado correctamente");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.ok("Contraseña actualizada correctamente");
    }
}
