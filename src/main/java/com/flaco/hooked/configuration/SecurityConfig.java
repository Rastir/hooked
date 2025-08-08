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

                        // ========== RUTAS PÚBLICAS - AUTENTICACIÓN (PRIMERA PRIORIDAD) ==========
                        .requestMatchers("/api/auth/**").permitAll()  // ← ESTO DEBE IR PRIMERO

                        // ========== OTRAS RUTAS PÚBLICAS ==========
                        .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comentarios/post/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // ⚡ USUARIOS - ENDPOINTS PÚBLICOS (LECTURA)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/especialidad/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/activos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/nivel/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/ubicacion/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/mas-activos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/nuevos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/buscar-avanzado").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/stats").permitAll()

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

                        // USUARIOS - OPERACIONES PRIVADAS
                        .requestMatchers("/api/usuarios/perfil").authenticated()
                        .requestMatchers("/api/usuarios/perfil/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").authenticated()

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