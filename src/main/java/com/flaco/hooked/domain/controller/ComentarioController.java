package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarComentarioRequest;
import com.flaco.hooked.domain.request.CrearComentarioRequest;
import com.flaco.hooked.domain.response.ComentarioResponse;
import com.flaco.hooked.domain.response.PaginatedResponse;
import com.flaco.hooked.domain.service.ComentarioService;
import com.flaco.hooked.model.Usuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

    // Crear comentario
    @PostMapping
    public ResponseEntity<ComentarioResponse> crearComentario(
            @Valid @RequestBody CrearComentarioRequest request,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();
        ComentarioResponse comentario = comentarioService.crearComentario(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(comentario);
    }

    // Obtener comentario específico
    @GetMapping("/{id}")
    public ResponseEntity<ComentarioResponse> obtenerComentario(@PathVariable Long id) {
        ComentarioResponse comentario = comentarioService.obtenerComentario(id);
        return ResponseEntity.ok(comentario);
    }

    // Actualizar comentario
    @PutMapping("/{id}")
    public ResponseEntity<ComentarioResponse> actualizarComentario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarComentarioRequest request,
            Authentication authentication) {

        Usuario usuario = (Usuario) authentication.getPrincipal();
        ComentarioResponse comentario = comentarioService.actualizarComentario(id, request, usuario);
        return ResponseEntity.ok(comentario);
    }

    // Eliminar comentario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarComentario(@PathVariable Long id, Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        comentarioService.eliminarComentario(id, usuario);
        return ResponseEntity.noContent().build();
    }

    // Obtener comentarios de un post - CON DETECCIÓN AUTOMÁTICA DE PAGINACIÓN
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> obtenerComentariosPorPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano,
            @RequestParam(required = false) String tipo) {

        // Si vienen parámetros de paginación, usar versión paginada
        if (pagina != null || tamano != null) {
            int paginaFinal = pagina != null ? pagina : 0;
            int tamanoFinal = tamano != null ? tamano : 20; // Default 20 para comentarios

            // Determinar tipo de comentarios a mostrar
            if ("principales".equals(tipo)) {
                // Solo comentarios principales (sin respuestas anidadas)
                PaginatedResponse<ComentarioResponse> comentarios =
                        comentarioService.obtenerComentariosPrincipalesPorPostPaginados(postId, paginaFinal, tamanoFinal);
                return ResponseEntity.ok(comentarios);
            } else {
                // Todos los comentarios (incluyendo respuestas)
                PaginatedResponse<ComentarioResponse> comentarios =
                        comentarioService.obtenerComentariosPorPostPaginados(postId, paginaFinal, tamanoFinal);
                return ResponseEntity.ok(comentarios);
            }
        } else {
            // Sin paginación - usar método original (COMPATIBILIDAD)
            List<ComentarioResponse> comentarios = comentarioService.obtenerComentariosPorPost(postId);
            return ResponseEntity.ok(comentarios);
        }
    }

    // Obtener comentarios de un usuario - CON DETECCIÓN AUTOMÁTICA DE PAGINACIÓN
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerComentariosPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano,
            @RequestParam(required = false) String tipo) {

        // Si vienen parámetros de paginación, usar versión paginada
        if (pagina != null || tamano != null) {
            int paginaFinal = pagina != null ? pagina : 0;
            int tamanoFinal = tamano != null ? tamano : 20;

            if ("recientes".equals(tipo)) {
                // Comentarios recientes para perfil
                PaginatedResponse<ComentarioResponse> comentarios =
                        comentarioService.obtenerComentariosRecientesPorUsuarioPaginados(usuarioId, paginaFinal, tamanoFinal);
                return ResponseEntity.ok(comentarios);
            } else {
                // Todos los comentarios del usuario
                PaginatedResponse<ComentarioResponse> comentarios =
                        comentarioService.obtenerComentariosPorUsuarioPaginados(usuarioId, paginaFinal, tamanoFinal);
                return ResponseEntity.ok(comentarios);
            }
        } else {
            // Sin paginación - método original
            List<ComentarioResponse> comentarios = comentarioService.obtenerComentariosPorUsuario(usuarioId);
            return ResponseEntity.ok(comentarios);
        }
    }

    // Obtener solo comentarios principales de un post - SOLO PAGINADO
    @GetMapping("/post/{postId}/principales")
    public ResponseEntity<PaginatedResponse<ComentarioResponse>> obtenerComentariosPrincipales(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {

        PaginatedResponse<ComentarioResponse> comentarios =
                comentarioService.obtenerComentariosPrincipalesPorPostPaginados(postId, pagina, tamano);
        return ResponseEntity.ok(comentarios);
    }

    // Obtener respuestas de un comentario específico - SOLO PAGINADO
    @GetMapping("/{comentarioId}/respuestas")
    public ResponseEntity<PaginatedResponse<ComentarioResponse>> obtenerRespuestas(
            @PathVariable Long comentarioId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        PaginatedResponse<ComentarioResponse> respuestas =
                comentarioService.obtenerRespuestasPaginadas(comentarioId, pagina, tamano);
        return ResponseEntity.ok(respuestas);
    }

    // Obtener comentarios recientes de un usuario (para perfil) - SOLO PAGINADO
    @GetMapping("/usuario/{usuarioId}/recientes")
    public ResponseEntity<PaginatedResponse<ComentarioResponse>> obtenerComentariosRecientes(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "15") int tamano) {

        PaginatedResponse<ComentarioResponse> comentarios =
                comentarioService.obtenerComentariosRecientesPorUsuarioPaginados(usuarioId, pagina, tamano);
        return ResponseEntity.ok(comentarios);
    }
}