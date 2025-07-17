package com.flaco.hooked.domain.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.flaco.hooked.domain.usuario.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class JwtService {

    @Value("${api.security.token.secret:secret-key-default}")
    private String llave;

    public String generarToken(Usuario usuario){
        try{
            Algorithm algorithm = Algorithm.HMAC256(llave);
            String token = JWT.create()
                    .withIssuer("hooked-api") // Issuer consistente
                    .withSubject(usuario.getEmail())
                    .withClaim("id", usuario.getId())
                    .withClaim("nombre", usuario.getNombre())
                    .withExpiresAt(generarFechaExpiracion())
                    .sign(algorithm);
            return token;
        } catch (JWTCreationException e){
            throw new RuntimeException("Error al generar el token JWT", e);
        }
    }

    public String validarToken(String token){
        try{
            Algorithm algorithm = Algorithm.HMAC256(llave);
            DecodedJWT verificador = JWT.require(algorithm)
                    .withIssuer("hooked-api") // CORREGIDO: mismo issuer que al generar
                    .build()
                    .verify(token); // CORREGIDO: verificar el token, no la llave
            return verificador.getSubject();
        }catch (JWTVerificationException e){
            throw new RuntimeException("Token JWT invalido o expirado", e);
        }
    }

    private Instant generarFechaExpiracion(){
        return LocalDateTime.now()
                .plusHours(24)
                .toInstant(ZoneOffset.of("-05:00")); // hora de Cancún
    }
}