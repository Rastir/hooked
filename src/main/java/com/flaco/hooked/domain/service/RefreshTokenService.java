package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.refreshtoken.RefreshToken;
import com.flaco.hooked.domain.refreshtoken.RefreshTokenException;
import com.flaco.hooked.domain.repository.RefreshTokenRepository;
import com.flaco.hooked.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final int MAX_TOKENS_POR_USUARIO = 2;
    private boolean esValido(String tokenValue) {
        return tokenValue != null && !tokenValue.trim().isEmpty();
    }

    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @Value("${hooked.jwt.refresh-expiration-seconds:2592000}") // 30 días
    private long refreshTokenDuracionSegundos;

    public RefreshToken crearRefreshToken(Usuario usuario, String dispositivoInfo, String ipAddress) {
        limpiarTokensExcesivos(usuario);

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsuario(usuario);
        token.setFechaCreacion(LocalDateTime.now());
        token.setFechaExpiracion(LocalDateTime.now().plusSeconds(refreshTokenDuracionSegundos));
        token.setDispositivoInfo(truncar(dispositivoInfo, 500));
        token.setIpAddress(ipAddress);
        token.setActivo(true);

        return refreshTokenRepository.save(token);
    }

    public Optional<RefreshToken> buscarPorToken(String tokenValue) {
        if (!esValido(tokenValue)) return Optional.empty();
        return refreshTokenRepository.findByTokenAndActivoTrue(tokenValue);
    }

    public RefreshToken verificarExpiracion(RefreshToken token) {
        if (token.isExpirado()) {
            revocarToken(token.getToken());
            throw new RefreshTokenException("Sesión expirada. Por favor, inicia sesión de nuevo.");
        }
        return token;
    }

    public void revocarToken(String tokenValue) {
        if (!esValido(tokenValue)) return;

        refreshTokenRepository.findByTokenAndActivoTrue(tokenValue)
                .ifPresent(this::desactivarToken);
    }

    public void revocarTodosTokensDelUsuario(Usuario usuario) {
        if (usuario == null) return;
        refreshTokenRepository.desactivarTodosTokensDelUsuario(usuario);
    }

    public List<RefreshToken> obtenerTokensActivosDelUsuario(Usuario usuario) {
        if (usuario == null) return List.of();
        return refreshTokenRepository.findByUsuarioAndActivoTrueOrderByFechaCreacionDesc(usuario);
    }

    public int contarTokensActivosDelUsuario(Usuario usuario) {
        if (usuario == null) return 0;
        return refreshTokenRepository.contarTokensActivosDelUsuario(usuario);
    }

    public boolean esTokenValido(String tokenValue) {
        return tokenValue != null
                && !tokenValue.trim().isEmpty()
                && buscarPorToken(tokenValue)
                .map(t -> t.isActivo() && !t.isExpirado())
                .orElse(false);
    }

    public List<SesionActivaInfo> obtenerSesionesActivas(Usuario usuario) {
        return obtenerTokensActivosDelUsuario(usuario).stream()
                .filter(t -> !t.isExpirado())
                .map(t -> new SesionActivaInfo(t, usuario))
                .toList();
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void limpiarTokensExcesivos(Usuario usuario) {
        int activos = contarTokensActivosDelUsuario(usuario);

        if (activos >= MAX_TOKENS_POR_USUARIO) {
            List<RefreshToken> tokens = refreshTokenRepository
                    .findByUsuarioAndActivoTrueOrderByFechaCreacionAsc(usuario);

            int aDesactivar = activos - MAX_TOKENS_POR_USUARIO + 1;

            tokens.stream()
                    .limit(aDesactivar)
                    .forEach(this::desactivarToken);
        }
    }

    private void desactivarToken(RefreshToken token) {
        token.setActivo(false);
        refreshTokenRepository.save(token);
    }

    private String truncar(String valor, int maxLength) {
        if (valor == null) return null;
        return valor.length() > maxLength ? valor.substring(0, maxLength) : valor;
    }

    @Scheduled(fixedRate = 86400000) // 24 horas
    public void limpiarTokensExpirados() {
        try {
            refreshTokenRepository.eliminarTokensExpiradosOInactivos(LocalDateTime.now());
            logger.info("Limpieza de refresh tokens completada");
        } catch (Exception e) {
            logger.error("Error al limpiar tokens expirados", e);
        }
    }

    // ========== RECORD PARA SESIONES ==========

    public record SesionActivaInfo(
            Long id,
            String dispositivo,
            String ip,
            LocalDateTime fechaCreacion,
            LocalDateTime fechaExpiracion,
            String nombreUsuario,
            String emailUsuario,
            String fotoPerfilUsuario
    ) {
        public SesionActivaInfo(RefreshToken token, Usuario usuario) {
            this(
                    token.getId(),
                    token.getDispositivoInfo(),
                    token.getIpAddress(),
                    token.getFechaCreacion(),
                    token.getFechaExpiracion(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    usuario.getFotoPerfil()
            );
        }
    }
}