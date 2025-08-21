package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.refreshtoken.RefreshToken;
import com.flaco.hooked.domain.refreshtoken.RefreshTokenException;
import com.flaco.hooked.domain.request.CrearUsuarioRequest;
import com.flaco.hooked.domain.request.LoginRequest;
import com.flaco.hooked.domain.request.LogoutRequest;
import com.flaco.hooked.domain.request.TokenRefreshRequest;
import com.flaco.hooked.domain.response.LoginResponse;
import com.flaco.hooked.domain.response.MessageResponse;
import com.flaco.hooked.domain.response.TokenRefreshResponse;
import com.flaco.hooked.domain.service.JwtService;
import com.flaco.hooked.domain.service.RefreshTokenService;
import com.flaco.hooked.domain.service.UsuarioService;
import com.flaco.hooked.domain.service.UtilsService;
import com.flaco.hooked.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UtilsService utilsService;

    @Value("${hooked.jwt.expiration:900000}")
    private long jwtExpirationMs;

    // Login con refresh token - NIVEL 2
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        try {
            // 🔥 NIVEL 2: Validar Content-Type
            String contentType = request.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .header("Accept", "application/json")
                        .body(MessageResponse.error("Content-Type debe ser application/json"));
            }

            // Tu lógica original de autenticación
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Autenticar (tu código original)
            Usuario usuario = (Usuario) authentication.getPrincipal();

            // Crear access token (tu código original)
            String accessToken = jwtService.generarToken(usuario);

            // NUEVO: Obtener información del dispositivo
            String dispositivoInfo = utilsService.obtenerInfoDispositivo(request);
            String ipAddress = utilsService.obtenerIPAddress(request);

            // NUEVO: Crear refresh token
            RefreshToken refreshToken = refreshTokenService.crearRefreshToken(
                    usuario, dispositivoInfo, ipAddress);

            // NUEVO: Respuesta con ambos tokens
            LoginResponse loginResponse = new LoginResponse(
                    accessToken,                    // Access token
                    refreshToken.getToken(),        // Refresh token
                    jwtExpirationMs / 1000,        // Expires in (segundos)
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getNombre()
            );

            // 🔥 NIVEL 2: Headers de seguridad profesionales
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Auth-Success", "true");
            headers.add("X-User-ID", usuario.getId().toString());
            headers.add("X-Session-Created", LocalDateTime.now().toString());
            headers.add("Cache-Control", "no-store");
            headers.add("Pragma", "no-cache");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(loginResponse); // ✅ 200 OK

        } catch (BadCredentialsException e) {
            // 🔥 NIVEL 2: 401 UNAUTHORIZED más específico
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Bearer")
                    .header("X-Auth-Error", "invalid-credentials")
                    .body(MessageResponse.error("Email o contraseña incorrectos"));

        } catch (DisabledException e) {
            // 🔥 NIVEL 2: 403 FORBIDDEN - Cuenta deshabilitada
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Auth-Error", "account-disabled")
                    .body(MessageResponse.error("Cuenta de usuario deshabilitada"));

        } catch (LockedException e) {
            // 🔥 NIVEL 2: 423 LOCKED - Cuenta bloqueada
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .header("X-Auth-Error", "account-locked")
                    .header("Retry-After", "3600") // 1 hora
                    .body(MessageResponse.error("Cuenta bloqueada temporalmente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MessageResponse.error("Error en los datos proporcionados"));
        }
    }

    // Registro con refresh token - NIVEL 2
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody CrearUsuarioRequest request,
                                      HttpServletRequest httpRequest,
                                      HttpServletResponse httpResponse) {
        try {
            // 🔥 NIVEL 2: Validar Content-Type
            String contentType = httpRequest.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .header("Accept", "application/json")
                        .body(MessageResponse.error("Content-Type debe ser application/json"));
            }

            // Tu lógica original: crear usuario (ya maneja validación de email duplicado)
            Usuario nuevoUsuario = usuarioService.crearUsuario(request);

            // Tu lógica original: crear token
            String accessToken = jwtService.generarToken(nuevoUsuario);

            // NUEVO: Obtener información del dispositivo
            String dispositivoInfo = utilsService.obtenerInfoDispositivo(httpRequest);
            String ipAddress = utilsService.obtenerIPAddress(httpRequest);

            // NUEVO: Crear refresh token
            RefreshToken refreshToken = refreshTokenService.crearRefreshToken(
                    nuevoUsuario, dispositivoInfo, ipAddress);

            // NUEVO: Respuesta con ambos tokens
            LoginResponse response = new LoginResponse(
                    accessToken,
                    refreshToken.getToken(),
                    jwtExpirationMs / 1000,
                    nuevoUsuario.getId(),
                    nuevoUsuario.getEmail(),
                    nuevoUsuario.getNombre()
            );

            // 🔥 NIVEL 2: Headers profesionales para registro
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-User-Created", "true");
            headers.add("X-User-ID", nuevoUsuario.getId().toString());
            headers.add("X-Welcome-Message", "Bienvenido a Hooked!");
            headers.add("Cache-Control", "no-store");

            // ✅ 201 CREATED con Location header optimizado
            return ResponseEntity.status(HttpStatus.CREATED)
                    .location(URI.create("/api/usuarios/" + nuevoUsuario.getId()))
                    .headers(headers)
                    .body(response);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // 🔥 NIVEL 2: 409 CONFLICT para recursos duplicados
            if (mensaje.contains("El email ya está registrado")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .header("X-Conflict-Field", "email")
                        .body(MessageResponse.error("Email ya está en uso"));
            }

            // Tu UsuarioService ya lanza RuntimeException para email duplicado y otras validaciones
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MessageResponse.error(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MessageResponse.error("Error al crear el usuario"));
        }
    }

    // Renovar access token usando refresh token - NIVEL 2
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request,
                                          HttpServletRequest httpRequest) {
        try {
            // 🔥 NIVEL 2: Validar Content-Type
            String contentType = httpRequest.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .header("Accept", "application/json")
                        .body(MessageResponse.error("Content-Type debe ser application/json"));
            }

            String refreshTokenValue = request.getRefreshToken();

            // Validar que el token no esté vacío
            if (refreshTokenValue == null || refreshTokenValue.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "missing-refresh-token")
                        .body(MessageResponse.error("Refresh token requerido"));
            }

            var tokenOpt = refreshTokenService.buscarPorToken(refreshTokenValue);

            if (tokenOpt.isEmpty()) {
                // 🔥 NIVEL 2: 403 FORBIDDEN más específico
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("WWW-Authenticate", "Bearer")
                        .header("X-Token-Error", "invalid-refresh-token")
                        .body(MessageResponse.error("Refresh token inválido o revocado"));
            }

            RefreshToken refreshToken = refreshTokenService.verificarExpiracion(tokenOpt.get());
            Usuario usuario = refreshToken.getUsuario();

            // Generar nuevo access token
            String newAccessToken = jwtService.generarToken(usuario);

            TokenRefreshResponse response = new TokenRefreshResponse(
                    newAccessToken,
                    refreshTokenValue,
                    jwtService.getExpirationMs() / 1000
            );

            // 🔥 NIVEL 2: Headers informativos
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Token-Refreshed", "true");
            headers.add("X-User-ID", usuario.getId().toString());
            headers.add("Cache-Control", "no-store");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response);

        } catch (RefreshTokenException e) {
            String mensaje = e.getMessage();

            // 🔥 NIVEL 2: Diferentes tipos de errores de token
            if (mensaje.contains("expirado")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Token-Error", "expired-refresh-token")
                        .header("WWW-Authenticate", "Bearer")
                        .body(MessageResponse.error("Refresh token expirado, inicia sesión nuevamente"));
            } else if (mensaje.contains("inválido")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Token-Error", "malformed-refresh-token")
                        .header("WWW-Authenticate", "Bearer")
                        .body(MessageResponse.error("Refresh token inválido"));
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-Token-Error", "refresh-token-error")
                    .body(MessageResponse.error(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error interno del servidor"));
        }
    }

    // Logout - Revocar refresh token específico - NIVEL 2
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request,
                                    HttpServletRequest httpRequest) {
        try {
            // 🔥 NIVEL 2: Validar Content-Type
            String contentType = httpRequest.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(MessageResponse.error("Content-Type debe ser application/json"));
            }

            // Validar que el token no esté vacío
            if (request.getRefreshToken() == null || request.getRefreshToken().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "missing-refresh-token")
                        .body(MessageResponse.error("Refresh token requerido"));
            }

            refreshTokenService.revocarToken(request.getRefreshToken());

            // 🔥 NIVEL 2: Headers informativos para logout
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Logout-Success", "true");
            headers.add("X-Session-Ended", LocalDateTime.now().toString());
            headers.add("Clear-Site-Data", "\"cache\", \"storage\"");

            // ✅ 204 NO CONTENT con headers informativos
            return ResponseEntity.noContent()
                    .headers(headers)
                    .build();

        } catch (Exception e) {
            // Incluso si hay error, el logout debería ser "exitoso" desde la perspectiva del cliente
            return ResponseEntity.noContent()
                    .header("X-Logout-Fallback", "true")
                    .build();
        }
    }

    // Logout de todos los dispositivos - NIVEL 2
    @PostMapping("/logout-all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> logoutFromAllDevices(Authentication authentication,
                                                  HttpServletRequest httpRequest) {
        try {
            // 🔥 NIVEL 2: Validar Content-Type
            String contentType = httpRequest.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .body(MessageResponse.error("Content-Type debe ser application/json"));
            }

            Usuario usuario = (Usuario) authentication.getPrincipal();

            // ✅ CORREGIDO: Tu método retorna void, no int
            refreshTokenService.revocarTodosTokensDelUsuario(usuario);

            // 🔥 NIVEL 2: Headers informativos (sin contar sesiones)
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Logout-All-Success", "true");
            headers.add("X-User-ID", usuario.getId().toString());
            headers.add("X-Action", "all-sessions-revoked");
            headers.add("Clear-Site-Data", "\"cache\", \"storage\"");

            return ResponseEntity.noContent()
                    .headers(headers)
                    .build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al cerrar sesiones"));
        }
    }

    // Ver sesiones activas del usuario - NIVEL 2
    @GetMapping("/sessions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSesionesActivas(Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            var sesiones = refreshTokenService.obtenerSesionesActivas(usuario);

            // 🔥 NIVEL 2: Headers informativos
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Total-Sessions", String.valueOf(sesiones.size()));
            headers.add("X-User-ID", usuario.getId().toString());
            headers.add("Cache-Control", "private, no-cache");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(sesiones);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener sesiones activas"));
        }
    }
}