package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.AuthRequest;
import AgroLink.AgroLink.domain.dto.AuthResponse;
import AgroLink.AgroLink.domain.repository.AgricultorRepository;
import AgroLink.AgroLink.domain.repository.CompradorRepository;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import AgroLink.AgroLink.persistance.entity.Agricultor;
import AgroLink.AgroLink.persistance.entity.Comprador;
import AgroLink.AgroLink.persistance.entity.Rol;
import AgroLink.AgroLink.persistance.entity.Usuario;
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
    private final AgricultorRepository agricultorRepository;
    private final CompradorRepository compradorRepository;

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

        emailService.sendVerificationEmail(request.getEmail(), codigo);

        return "Código de verificación enviado a " + request.getEmail();
    }

    public AuthResponse verifyEmail(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getCodigoVerificacion().equals(codigo)) {
            throw new RuntimeException("Código incorrecto");
        }

        usuario.setVerificado(true);
        usuario.setCodigoVerificacion(null);
        usuarioRepository.save(usuario);

        // Crear perfil según rol
        if (usuario.getRol() == Rol.AGRICULTOR) {
            boolean yaExiste = agricultorRepository.findByUsuario(usuario).isPresent();
            if (!yaExiste) {
                Agricultor agricultor = new Agricultor();
                agricultor.setUsuario(usuario);
                agricultorRepository.save(agricultor);
            }
        } else if (usuario.getRol() == Rol.COMPRADOR) {
            boolean yaExiste = compradorRepository.findByUsuario(usuario).isPresent();
            if (!yaExiste) {
                Comprador comprador = new Comprador();
                comprador.setUsuario(usuario);
                compradorRepository.save(comprador);
            }
        }

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

    public String registerAdmin(AuthRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(request.getNombres());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.ADMIN);
        usuario.setVerificado(true); // Admin no necesita verificar email
        usuarioRepository.save(usuario);

        return "Admin creado correctamente";
    }
}
