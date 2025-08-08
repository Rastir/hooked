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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    /**
     * LOGIN - Ahora con refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                               HttpServletRequest request) {
        try {
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
            LoginResponse response = new LoginResponse(
                    accessToken,                    // Access token
                    refreshToken.getToken(),        // Refresh token
                    jwtExpirationMs / 1000,        // Expires in (segundos)
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getNombre()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse()); // Respuesta vacía en caso de error
        }
    }

    /**
     * REGISTRO - Ahora con refresh token
     */
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(@Valid @RequestBody CrearUsuarioRequest request,
                                                  HttpServletRequest httpRequest) {
        try {
            // Tu lógica original: crear usuario
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

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse()); // Respuesta vacía en caso de error
        }
    }

    /**
     * NUEVO: Renovar access token usando refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            String refreshTokenValue = request.getRefreshToken();

            var tokenOpt = refreshTokenService.buscarPorToken(refreshTokenValue);

            if (tokenOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(MessageResponse.error("Refresh token inválido"));
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

            return ResponseEntity.ok(response);

        } catch (RefreshTokenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(MessageResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error interno del servidor"));
        }
    }

    /**
     * NUEVO: Logout - Revocar refresh token específico
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody LogoutRequest request) {
        try {
            refreshTokenService.revocarToken(request.getRefreshToken());
            return ResponseEntity.ok(MessageResponse.success("Sesión cerrada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.ok(MessageResponse.success("Sesión cerrada")); // Siempre exitoso
        }
    }

    /**
     * NUEVO: Logout de todos los dispositivos
     */
    @PostMapping("/logout-all")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> logoutFromAllDevices(Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            refreshTokenService.revocarTodosTokensDelUsuario(usuario);
            return ResponseEntity.ok(MessageResponse.success("Sesión cerrada en todos los dispositivos"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al cerrar sesiones"));
        }
    }

    /**
     * NUEVO: Ver sesiones activas del usuario
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getSesionesActivas(Authentication authentication) {
        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            var sesiones = refreshTokenService.obtenerSesionesActivas(usuario);
            return ResponseEntity.ok(sesiones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener sesiones activas"));
        }
    }
}