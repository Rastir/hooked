package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.refreshtoken.RefreshToken;
import com.flaco.hooked.domain.refreshtoken.RefreshTokenException;
import com.flaco.hooked.domain.repository.RefreshTokenRepository;
import com.flaco.hooked.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // Configuración: 30 días por defecto para refresh tokens
    @Value("${hooked.jwt.refresh-expiration-seconds:2592000}")
    private long refreshTokenDuracionSegundos;

    // Máximo 2 dispositivos conectados por usuario
    private static final int MAX_TOKENS_POR_USUARIO = 2;

    // Crear un nuevo refresh token para un usuario
    public RefreshToken crearRefreshToken(Usuario usuario, String dispositivoInfo, String ipAddress) {
        // Primero limpiamos tokens excesivos si los hay
        limpiarTokensExcesivos(usuario);

        // Crear el nuevo token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsuario(usuario);
        refreshToken.setFechaCreacion(LocalDateTime.now());
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusSeconds(refreshTokenDuracionSegundos));
        refreshToken.setDispositivoInfo(dispositivoInfo != null ? dispositivoInfo.substring(0, Math.min(500, dispositivoInfo.length())) : null);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setActivo(true);

        return refreshTokenRepository.save(refreshToken);
    }

    // Buscar refresh token activo por valor
    public Optional<RefreshToken> buscarPorToken(String tokenValue) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            return Optional.empty();
        }
        return refreshTokenRepository.findByTokenAndActivoTrue(tokenValue);
    }

    // Verificar si el token está expirado y manejarlo
    public RefreshToken verificarExpiracion(RefreshToken token) {
        if (token.isExpirado()) {
            // Desactivar el token expirado
            token.setActivo(false);
            refreshTokenRepository.save(token);
            throw new RefreshTokenException("Sesión expirada. Por favor, inicia sesión de nuevo.");
        }
        return token;
    }

    // Revocar un refresh token específico
    public void revocarToken(String tokenValue) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            return;
        }

        refreshTokenRepository.findByTokenAndActivoTrue(tokenValue)
                .ifPresent(token -> {
                    token.setActivo(false);
                    refreshTokenRepository.save(token);
                });
    }

    // Revocar todos los refresh tokens de un usuario (logout de todos los dispositivos)
    public void revocarTodosTokensDelUsuario(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        refreshTokenRepository.desactivarTodosTokensDelUsuario(usuario);
    }

    // Obtener todos los refresh tokens activos de un usuario
    public List<RefreshToken> obtenerTokensActivosDelUsuario(Usuario usuario) {
        if (usuario == null) {
            return List.of();
        }
        return refreshTokenRepository.findByUsuarioAndActivoTrueOrderByFechaCreacionDesc(usuario);
    }

    // Contar tokens activos de un usuario
    public int contarTokensActivosDelUsuario(Usuario usuario) {
        if (usuario == null) {
            return 0;
        }
        return refreshTokenRepository.contarTokensActivosDelUsuario(usuario);
    }

    // Limpiar tokens excesivos para no sobrepasar el límite
    private void limpiarTokensExcesivos(Usuario usuario) {
        int tokensActivos = contarTokensActivosDelUsuario(usuario);

        if (tokensActivos >= MAX_TOKENS_POR_USUARIO) {
            // Obtener todos los tokens activos ordenados por fecha (más viejos primero)
            List<RefreshToken> tokens = refreshTokenRepository.findByUsuarioAndActivoTrueOrderByFechaCreacionDesc(usuario);

            // Ordenar por fecha de creación (más viejos primero)
            tokens.sort(Comparator.comparing(RefreshToken::getFechaCreacion));

            // Calcular cuántos tokens debemos desactivar
            int tokensADesactivar = tokensActivos - MAX_TOKENS_POR_USUARIO + 1;

            // Desactivar los tokens más viejos
            for (int i = 0; i < Math.min(tokensADesactivar, tokens.size()); i++) {
                RefreshToken tokenViejo = tokens.get(i);
                tokenViejo.setActivo(false);
                refreshTokenRepository.save(tokenViejo);
            }
        }
    }

    // Tarea programada: Limpiar tokens expirados cada 24 hora, se ejecuta automáticamente
    @Scheduled(fixedRate = 86400000) // 24 horas en milisegundos
    public void limpiarTokensExpirados() {
        try {
            LocalDateTime fechaLimite = LocalDateTime.now();
            refreshTokenRepository.eliminarTokensExpiradosOInactivos(fechaLimite);
            System.out.println(" Limpieza de refresh tokens completada: " + fechaLimite);
        } catch (Exception e) {
            System.err.println(" Error al limpiar tokens expirados: " + e.getMessage());
        }
    }

    // Validar si un refresh token es válido y activo
    public boolean esTokenValido(String tokenValue) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            return false;
        }

        Optional<RefreshToken> tokenOpt = buscarPorToken(tokenValue);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        RefreshToken token = tokenOpt.get();
        return token.isActivo() && !token.isExpirado();
    }

    // Obtener información de sesiones activas para un usuario (para mostrar en UI)
    public List<SesionActivaInfo> obtenerSesionesActivas(Usuario usuario) {
        List<RefreshToken> tokens = obtenerTokensActivosDelUsuario(usuario);

        return tokens.stream()
                .filter(token -> !token.isExpirado())
                .map(token -> new SesionActivaInfo(
                        token.getId(),
                        token.getDispositivoInfo(),
                        token.getIpAddress(),
                        token.getFechaCreacion(),
                        token.getFechaExpiracion(),
                        // Info del usuario
                        usuario.getNombre(),
                        usuario.getEmail(),
                        usuario.getFotoPerfil()
                ))
                .toList();
    }

    // Clase interna para información de sesiones
    public static class SesionActivaInfo {
        private final Long id;
        private final String dispositivo;
        private final String ip;
        private final LocalDateTime fechaCreacion;
        private final LocalDateTime fechaExpiracion;
        // NUEVOS CAMPOS
        private final String nombreUsuario;
        private final String emailUsuario;
        private final String fotoPerfilUsuario;

        // Constructor original (mantener compatibilidad)
        public SesionActivaInfo(Long id, String dispositivo, String ip,
                                LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion) {
            this.id = id;
            this.dispositivo = dispositivo;
            this.ip = ip;
            this.fechaCreacion = fechaCreacion;
            this.fechaExpiracion = fechaExpiracion;
            this.nombreUsuario = null;
            this.emailUsuario = null;
            this.fotoPerfilUsuario = null;
        }

        public SesionActivaInfo(Long id, String dispositivo, String ip,
                                LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion,
                                String nombreUsuario, String emailUsuario, String fotoPerfilUsuario) {
            this.id = id;
            this.dispositivo = dispositivo;
            this.ip = ip;
            this.fechaCreacion = fechaCreacion;
            this.fechaExpiracion = fechaExpiracion;
            this.nombreUsuario = nombreUsuario;
            this.emailUsuario = emailUsuario;
            this.fotoPerfilUsuario = fotoPerfilUsuario;
        }

        // Getters originales
        public Long getId() { return id; }
        public String getDispositivo() { return dispositivo; }
        public String getIp() { return ip; }
        public LocalDateTime getFechaCreacion() { return fechaCreacion; }
        public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }

        // NUEVOS Getters
        public String getNombreUsuario() { return nombreUsuario; }
        public String getEmailUsuario() { return emailUsuario; }
        public String getFotoPerfilUsuario() { return fotoPerfilUsuario; }
    }
}