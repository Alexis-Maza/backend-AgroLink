package AgroLink.AgroLink.domain.service;

import AgroLink.AgroLink.domain.dto.AuthRequest;
import AgroLink.AgroLink.domain.dto.AuthResponse;
import AgroLink.AgroLink.domain.repository.UsuarioRepository;
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

    public AuthResponse register(AuthRequest request) {
        // Validar que no se registre como ADMIN
        if (request.getRol().equalsIgnoreCase("ADMIN")) {
            throw new RuntimeException("No puedes registrarte como ADMIN");
        }

        // 1. Crear el usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.valueOf(request.getRol()));

        // 2. Guardarlo en la BD
        usuarioRepository.save(usuario);

        // 3. Generar y devolver el token
        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token);
    }

    public AuthResponse login(AuthRequest request) {
        // 1. Verificar credenciales (lanza excepción si son incorrectas)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Buscar el usuario
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // 3. Generar y devolver el token
        String token = jwtService.generateToken(usuario);
        return new AuthResponse(token);
    }
}
