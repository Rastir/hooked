package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarPerfilRequest;
import com.flaco.hooked.domain.response.UsuarioResponse;
import com.flaco.hooked.domain.response.PaginatedResponse; // ⚡ NUEVO IMPORT
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

    // ==========  BÚSQUEDA CON PAGINACIÓN AUTOMATICA ==========

    // LISTAR/BUSCAR USUARIOS (con detección automática de paginación)
    @GetMapping
    public ResponseEntity<?> listarUsuarios(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano) {

        try {
            // DETECCIÓN AUTOMÁTICA: Si vienen parámetros de paginación -> usar versión paginada
            if (pagina != null || tamano != null) {
                // Valores por defecto para paginación
                int paginaFinal = (pagina != null) ? pagina : 0;
                int tamanoFinal = (tamano != null) ? tamano : 10;

                // Usar servicio paginado
                PaginatedResponse<UsuarioResponse> usuarios = usuarioService.buscarUsuariosPaginados(
                        buscar, paginaFinal, tamanoFinal);
                return ResponseEntity.ok(usuarios);

            } else {
                //Sin parámetros -> comportamiento original
                List<UsuarioResponse> usuarios = usuarioService.buscarUsuarios(buscar);
                return ResponseEntity.ok(usuarios);
            }

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Usuarios por especialidad/tag específico
    @GetMapping("/especialidad/{tag}")
    public ResponseEntity<?> obtenerUsuariosPorTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosPorTagPaginados(
                    tag, pagina, tamano);
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Usuarios activos (con actividad reciente)
    @GetMapping("/activos")
    public ResponseEntity<?> obtenerUsuariosActivos(
            @RequestParam(defaultValue = "30") int dias,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosActivosPaginados(
                    dias, pagina, tamano);
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Usuarios por nivel de experiencia
    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<?> obtenerUsuariosPorNivel(
            @PathVariable String nivel,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosPorNivelPaginados(
                    nivel, pagina, tamano);
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Usuarios por ubicación
    @GetMapping("/ubicacion/{ubicacion}")
    public ResponseEntity<?> obtenerUsuariosPorUbicacion(
            @PathVariable String ubicacion,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosPorUbicacionPaginados(
                    ubicacion, pagina, tamano);
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Usuarios más activos (con más posts)
    @GetMapping("/mas-activos")
    public ResponseEntity<?> obtenerUsuariosMasActivos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosMasActivosPaginados(
                    pagina, tamano);
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Usuarios nuevos (registrados recientemente)
    @GetMapping("/nuevos")
    public ResponseEntity<?> obtenerUsuariosNuevos(
            @RequestParam(defaultValue = "7") int dias,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosNuevosPaginados(
                    dias, pagina, tamano);
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Búsqueda avanzada (busca en múltiples campos)
    @GetMapping("/buscar-avanzado")
    public ResponseEntity<?> busquedaAvanzada(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.busquedaAvanzadaPaginada(
                    q, pagina, tamano);
            return ResponseEntity.ok(usuarios);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
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