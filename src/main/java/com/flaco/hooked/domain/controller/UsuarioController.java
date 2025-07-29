package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarPerfilRequest;
import com.flaco.hooked.domain.response.UsuarioResponse;
import com.flaco.hooked.domain.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    //PERFIL DEL USUARIO AUTENTICADO
    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> obtenerMiPerfil(Authentication authentication) {
        try {
            String email = authentication.getName();
            UsuarioResponse usuario = usuarioService.obtenerPerfilPorEmail(email);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // VER PERFIL PÚBLICO DE OTRO USUARIO
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPerfilPublico(@PathVariable Long id) {
        try {
            UsuarioResponse usuario = usuarioService.obtenerPerfilPublico(id);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //ACTUALIZAR MI PERFIL
    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarMiPerfil(
            @Valid @RequestBody ActualizarPerfilRequest request,
            BindingResult bindingResult,
            Authentication authentication) {

        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            Map<String, String> errores = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errores.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errores);
        }

        try {
            String email = authentication.getName();
            UsuarioResponse usuario = usuarioService.actualizarPerfil(email, request);
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // BUSCAR USUARIOS
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios(
            @RequestParam(required = false) String buscar) {
        try {
            List<UsuarioResponse> usuarios = usuarioService.buscarUsuarios(buscar);
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // FOTO DE PERFIL
    @PostMapping("/perfil/foto")
    public ResponseEntity<?> subirFotoPerfil(
            @RequestParam("foto") MultipartFile archivo,
            Authentication authentication) {

        try {
            String email = authentication.getName();
            String fotoUrl = usuarioService.subirFotoPerfil(email, archivo);

            Map<String, String> response = new HashMap<>();
            response.put("fotoUrl", fotoUrl);
            response.put("mensaje", "Foto de perfil actualizada exitosamente");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ESTADÍSTICAS PAL' DASHBOARD
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsuarios", usuarioService.contarUsuarios());
            // Futuras estadísticas aquí

            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}