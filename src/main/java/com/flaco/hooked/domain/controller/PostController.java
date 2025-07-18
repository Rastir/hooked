package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarPostRequest;
import com.flaco.hooked.domain.request.CrearPostRequest;
import com.flaco.hooked.domain.response.PostResponse;
import com.flaco.hooked.domain.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    // Crear un nuevo post
    @PostMapping
    public ResponseEntity<PostResponse> crearPost(
            @Valid @RequestBody CrearPostRequest request,
            Authentication authentication) {

        String emailUsuario = authentication.getName(); // Email del JWT
        PostResponse postCreado = postService.crearPost(request, emailUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(postCreado);
    }

    // Obtener todos los posts
    @GetMapping
    public ResponseEntity<List<PostResponse>> obtenerTodosLosPosts() {
        List<PostResponse> posts = postService.obtenerTodosPosts();
        return ResponseEntity.ok(posts);
    }

    // Obtener post por ID
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> obtenerPostPorId(@PathVariable Long id) {
        PostResponse post = postService.obtenerPostPorId(id);
        return ResponseEntity.ok(post);
    }

    // Obtener posts por usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PostResponse>> obtenerPostsPorUsuario(@PathVariable Long usuarioId) {
        List<PostResponse> posts = postService.obtenerPostsPorUsuario(usuarioId);
        return ResponseEntity.ok(posts);
    }

    // Obtener posts por categoría
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<PostResponse>> obtenerPostsPorCategoria(@PathVariable Long categoriaId) {
        List<PostResponse> posts = postService.obtenerPostsPorCategoria(categoriaId);
        return ResponseEntity.ok(posts);
    }

    // Actualizar post
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> actualizarPost(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPostRequest request,
            Authentication authentication) {

        String emailUsuario = authentication.getName();

        // Debug temporal
        System.out.println("=== DEBUG ACTUALIZAR POST ===");
        System.out.println("ID del post: " + id);
        System.out.println("Email del usuario autenticado: " + emailUsuario);
        System.out.println("Request: " + request);

        PostResponse postActualizado = postService.actualizarPost(id, request, emailUsuario);
        return ResponseEntity.ok(postActualizado);
    }

    // Eliminar post
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPost(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            postService.eliminarPost(id, emailUsuario);

            // Crear una respuesta con mensaje
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Post eliminado exitosamente");
            response.put("id", id.toString());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Incrementar likes de un post
    @PostMapping("/{id}/like")
    public ResponseEntity<?> darLike(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            PostResponse post = postService.darLike(id, emailUsuario);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Agregar método para quitar like
    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> quitarLike(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            PostResponse post = postService.quitarLike(id, emailUsuario);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Obtener mis posts (del usuario autenticado)
    @GetMapping("/mis-posts")
    public ResponseEntity<List<PostResponse>> obtenerMisPosts(Authentication authentication) {
        String emailUsuario = authentication.getName();
        // Primero necesitamos obtener el ID del usuario por su email
        // Esto lo podríamos agregar al servicio
        return ResponseEntity.ok(postService.obtenerPostsPorUsuario(emailUsuario));
    }
}