package AgroLink.AgroLink.web.config;

import AgroLink.AgroLink.domain.service.UsuarioDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Habilita @PreAuthorize / @PostAuthorize en controladores
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final UsuarioDetailsService usuarioDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll() 
                        .requestMatchers("/productos/**").permitAll()
                        .requestMatchers("/reportes/mis-cultivos/**", "/reportes/mis-ventas/**").hasAuthority("AGRICULTOR")
                        .requestMatchers("/reportes/mis-compras/**").hasAuthority("COMPRADOR")
                        .requestMatchers("/cultivos/disponibles").hasAuthority("COMPRADOR") // ← primero la específica
                        .requestMatchers("/cultivos/**").hasAuthority("AGRICULTOR")         // ← luego la general
                        .requestMatchers("/agricultor/**").hasAuthority("AGRICULTOR")
                        .requestMatchers("/comprador/**").hasAuthority("COMPRADOR")
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        .requestMatchers("/ws-agrolink/**").permitAll()

                        // Trazabilidad: accesible por AGRICULTOR (su cultivo) y COMPRADOR (catálogo)
                        .requestMatchers("/api/v1/trazabilidad/**").hasAnyAuthority("AGRICULTOR", "COMPRADOR")

                        // Historial de pedidos: accesible por AGRICULTOR y COMPRADOR
                        .requestMatchers("/api/v1/pedidos/**").hasAnyAuthority("AGRICULTOR", "COMPRADOR")

                         // Permitir Swagger sin autenticación
                        .requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**",
                            "/swagger-resources/**",
                            "/webjars/**"
                        ).permitAll()
                        
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "https://agrolink-frontend.onrender.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE","PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}