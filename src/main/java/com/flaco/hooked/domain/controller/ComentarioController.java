package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarComentarioRequest;
import com.flaco.hooked.domain.request.CrearComentarioRequest;
import com.flaco.hooked.domain.response.ComentarioResponse;
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

    // Obtener comentarios de un post
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<ComentarioResponse>> obtenerComentariosPorPost(@PathVariable Long postId) {
        List<ComentarioResponse> comentarios = comentarioService.obtenerComentariosPorPost(postId);
        return ResponseEntity.ok(comentarios);
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

    // Obtener comentarios de un usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ComentarioResponse>> obtenerComentariosPorUsuario(@PathVariable Long usuarioId) {
        List<ComentarioResponse> comentarios = comentarioService.obtenerComentariosPorUsuario(usuarioId);
        return ResponseEntity.ok(comentarios);
    }
}