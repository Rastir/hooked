package com.flaco.hooked.configuration;

import com.flaco.hooked.domain.response.MessageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MessageResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", "Bearer")
                .header("X-Auth-Error", "invalid-credentials")
                .body(MessageResponse.error("Email o contraseña incorrectos"));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<MessageResponse> handleDisabled(DisabledException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header("X-Auth-Error", "account-disabled")
                .body(MessageResponse.error("Cuenta de usuario deshabilitada"));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<MessageResponse> handleLocked(LockedException e) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .header("X-Auth-Error", "account-locked")
                .header("Retry-After", "3600")
                .body(MessageResponse.error("Cuenta bloqueada temporalmente"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleRuntime(RuntimeException e) {
        String msg = e.getMessage();

        if (msg.contains("no encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(MessageResponse.error(msg));
        }
        if (msg.contains("permisos")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(MessageResponse.error(msg));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MessageResponse.error(msg));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MessageResponse> handleIllegalArgument(IllegalArgumentException e) {
        String msg = e.getMessage();

        if (msg.contains("ID") || msg.contains("id")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Validation-Error", "invalid-id")
                    .body(MessageResponse.error(msg));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MessageResponse.error(msg));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponse> handleNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header("X-Resource-Error", "not-found")
                .body(MessageResponse.error(e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header("X-Permission-Error", "access-denied")
                .body(MessageResponse.error("No tienes permiso para realizar esta acción"));
    }
}