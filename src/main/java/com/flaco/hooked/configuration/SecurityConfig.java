package com.flaco.hooked.configuration;

import com.flaco.hooked.domain.filter.JwtAuthenticationFilter;
import com.flaco.hooked.domain.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ========== RUTAS PÚBLICAS ==========
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comentarios/post/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // ⚡ NUEVOS ENDPOINTS PÚBLICOS DE USUARIOS (LECTURA)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").permitAll() // Buscar usuarios (con/sin paginación)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}").permitAll() // Ver perfil público
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/especialidad/**").permitAll() // Por tag
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/activos").permitAll() // Usuarios activos
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/nivel/**").permitAll() // Por nivel
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/ubicacion/**").permitAll() // Por ubicación
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/mas-activos").permitAll() // Más activos
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/nuevos").permitAll() // Nuevos
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/buscar-avanzado").permitAll() // Búsqueda avanzada
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/stats").permitAll() // Estadísticas públicas

                        // ========== RUTAS QUE REQUIEREN AUTENTICACIÓN ==========

                        // Posts (CREATE, UPDATE, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()

                        // Categorías (CREATE, UPDATE, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/categorias").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").authenticated()

                        // Comentarios (CREATE, UPDATE, DELETE)
                        .requestMatchers(HttpMethod.POST, "/api/comentarios").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/comentarios/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comentarios/**").authenticated()

                        // USUARIOS - OPERACIONES PRIVADAS (requieren autenticación)
                        .requestMatchers("/api/usuarios/perfil").authenticated() // Mi perfil (GET y PUT)
                        .requestMatchers("/api/usuarios/perfil/**").authenticated() // Subir foto, etc.
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/**").authenticated() // Cualquier POST
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").authenticated() // Cualquier PUT
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").authenticated() // Cualquier DELETE

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Para desarrollo
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}