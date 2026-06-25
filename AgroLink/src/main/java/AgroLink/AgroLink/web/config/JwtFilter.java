package AgroLink.AgroLink.web.config;

import AgroLink.AgroLink.domain.repository.UsuarioRepository;
import AgroLink.AgroLink.domain.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Leer el header Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Si no tiene token, dejar pasar (SecurityConfig decidirá)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token (quitar el "Bearer " del inicio)
        String token = authHeader.substring(7);

        // 4. Validar el token
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extraer el email y buscar el usuario
        String email = jwtService.extractEmail(token);
        UserDetails usuario = usuarioRepository.findByEmail(email).orElseThrow();

        // 6. Registrar al usuario como autenticado en Spring
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 7. Continuar con el request
        filterChain.doFilter(request, response);
    }
}
