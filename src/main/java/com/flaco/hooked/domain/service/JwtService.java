package com.flaco.hooked.domain.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.flaco.hooked.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${api.security.token.secret}")
    private String secretKey;

    @Value("${hooked.jwt.expiration:900000}") // 15 min default
    private long jwtExpirationMs;

    private static final String ISSUER = "hooked-api";
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("-06:00"); // CDMX (ajusta a tu zona)

    public String generarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getEmail())
                    .withClaim("userId", usuario.getId())
                    .withClaim("nombre", usuario.getNombre())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(generarFechaExpiracion())
                    .sign(algorithm);

        } catch (JWTCreationException e) {
            logger.error("Error al generar token para usuario: {}", usuario.getEmail());
            throw new RuntimeException("Error al generar el token JWT", e);
        }
    }

    /**
     * Valida token y retorna email (subject).
     * @return email si válido, null si inválido (no lanza excepción)
     */
    public String validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);

            return decodedJWT.getSubject();

        } catch (JWTVerificationException e) {
            logger.warn("Token inválido o expirado: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrae claims sin validar (útil si ya validaste antes).
     */
    public DecodedJWT decodificarToken(String token) {
        return JWT.decode(token);
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    private Instant generarFechaExpiracion() {
        return LocalDateTime.now()
                .plusSeconds(jwtExpirationMs / 1000)
                .toInstant(ZONE_OFFSET);
    }
}
