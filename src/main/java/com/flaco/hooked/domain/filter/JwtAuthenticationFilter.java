package com.flaco.hooked.domain.filter;

import com.flaco.hooked.domain.service.CustomUserDetailsService;
import com.flaco.hooked.domain.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("=== JWT Filter Debug ===");
        System.out.println("Request URI: " + request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        System.out.println("Auth Header: " + authHeader);

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Esto debería quitar "Bearer "
            System.out.println("Token extraído (sin Bearer): " + token.substring(0, Math.min(token.length(), 20)) + "...");

            try {
                String email = jwtService.validarToken(token);
                System.out.println("Email extraído del token: " + email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    System.out.println("UserDetails cargado: " + userDetails.getUsername());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("Autenticación establecida");
                }
            } catch (Exception e) {
                logger.error("Error al validar el token: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("=== Fin JWT Filter ===");
        filterChain.doFilter(request,response);
    }
}