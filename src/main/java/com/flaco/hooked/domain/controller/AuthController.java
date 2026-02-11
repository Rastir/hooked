package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.refreshtoken.RefreshToken;
import com.flaco.hooked.domain.request.*;
import com.flaco.hooked.domain.response.*;
import com.flaco.hooked.domain.service.*;
import com.flaco.hooked.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UsuarioService usuarioService;
    @Autowired private JwtService jwtService;
    @Autowired private RefreshTokenService refreshTokenService;
    @Autowired private UtilsService utilsService;

    @Value("${hooked.jwt.expiration:900000}")
    private long jwtExpirationMs;

    // ========== LOGIN ==========
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getContrasena())
        );

        Usuario usuario = extractUsuario(auth);
        AuthTokens tokens = generateTokens(usuario, httpRequest);

        return buildLoginResponse(tokens, usuario, HttpStatus.OK);
    }

    // ========== REGISTRO ==========
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(
            @Valid @RequestBody CrearUsuarioRequest request,
            HttpServletRequest httpRequest) {

        Usuario nuevoUsuario = usuarioService.crearUsuario(request);
        AuthTokens tokens = generateTokens(nuevoUsuario, httpRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Created", "true");
        headers.add("X-Welcome-Message", "Bienvenido a Hooked!");

        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/api/usuarios/" + nuevoUsuario.getId()))
                .headers(headers)
                .body(buildLoginResponseBody(tokens, nuevoUsuario));
    }

    // ========== REFRESH TOKEN ==========
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request) {

        RefreshToken refreshToken = refreshTokenService.verificarExpiracion(
                refreshTokenService.buscarPorToken(request.getRefreshToken())
                        .orElseThrow(() -> new RuntimeException("Refresh token inválido"))
        );

        Usuario usuario = refreshToken.getUsuario();
        String newAccessToken = jwtService.generarToken(usuario);

        TokenRefreshResponse response = new TokenRefreshResponse(
                newAccessToken,
                request.getRefreshToken(),
                jwtExpirationMs / 1000
        );

        return ResponseEntity.ok()
                .header("X-Token-Refreshed", "true")
                .body(response);
    }

    // ========== LOGOUT ==========
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revocarToken(request.getRefreshToken());

        return ResponseEntity.noContent()
                .header("X-Logout-Success", "true")
                .header("Clear-Site-Data", "\"cache\", \"storage\"")
                .build();
    }

    // ========== LOGOUT ALL ==========
    @PostMapping("/logout-all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        Usuario usuario = extractUsuario(authentication);
        refreshTokenService.revocarTodosTokensDelUsuario(usuario);

        return ResponseEntity.noContent()
                .header("X-Logout-All-Success", "true")
                .header("Clear-Site-Data", "\"cache\", \"storage\"")
                .build();
    }

    // ========== SESIONES ACTIVAS ==========
    @GetMapping("/sessions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getSesionesActivas(Authentication authentication) {
        Usuario usuario = extractUsuario(authentication);
        var sesiones = refreshTokenService.obtenerSesionesActivas(usuario);

        return ResponseEntity.ok()
                .header("X-Total-Sessions", String.valueOf(sesiones.size()))
                .body(sesiones);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Usuario extractUsuario(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        throw new RuntimeException("Tipo de usuario no soportado");
    }

    private AuthTokens generateTokens(Usuario usuario, HttpServletRequest request) {
        String accessToken = jwtService.generarToken(usuario);
        String dispositivo = utilsService.obtenerInfoDispositivo(request);
        String ip = utilsService.obtenerIPAddress(request);

        RefreshToken refreshToken = refreshTokenService.crearRefreshToken(
                usuario, dispositivo, ip
        );

        return new AuthTokens(accessToken, refreshToken.getToken());
    }

    private ResponseEntity<LoginResponse> buildLoginResponse(
            AuthTokens tokens, Usuario usuario, HttpStatus status) {

        LoginResponse body = buildLoginResponseBody(tokens, usuario);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Auth-Success", "true");
        headers.add("X-User-ID", usuario.getId().toString());
        headers.add("Cache-Control", "no-store");

        return ResponseEntity.status(status).headers(headers).body(body);
    }

    private LoginResponse buildLoginResponseBody(AuthTokens tokens, Usuario usuario) {
        return new LoginResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                jwtExpirationMs / 1000,
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre()
        );
    }

    // Record para tokens
    private record AuthTokens(String accessToken, String refreshToken) {}
}