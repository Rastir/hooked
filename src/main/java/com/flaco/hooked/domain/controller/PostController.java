package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarPostRequest;
import com.flaco.hooked.domain.request.CrearPostRequest;
import com.flaco.hooked.domain.response.PostResponse;
import com.flaco.hooked.domain.response.PaginatedResponse;
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

    // Obtener post por ID
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> obtenerPostPorId(@PathVariable Long id) {
        PostResponse post = postService.obtenerPostPorId(id);
        return ResponseEntity.ok(post);
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
        return ResponseEntity.ok(postService.obtenerPostsPorUsuario(emailUsuario));
    }

    // ENDPOINTS CON PAGINACIÓN (NUEVOS)

    // Obtener todos los posts - CON DETECCIÓN AUTOMÁTICA DE PAGINACIÓN
    @GetMapping
    public ResponseEntity<?> obtenerPosts(
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String buscar) {

        // Si vienen parámetros de paginación, usar versión paginada
        if (pagina != null || tamano != null) {
            int paginaFinal = pagina != null ? pagina : 0;
            int tamanoFinal = tamano != null ? tamano : 10;

            // Determinar qué tipo de consulta hacer
            if (buscar != null && !buscar.trim().isEmpty()) {
                // Búsqueda paginada
                PaginatedResponse<PostResponse> posts = postService.buscarPostsPaginados(
                        buscar.trim(), paginaFinal, tamanoFinal);
                return ResponseEntity.ok(posts);
            } else if (categoriaId != null) {
                // Filtro por categoría paginado
                PaginatedResponse<PostResponse> posts = postService.obtenerPostsPorCategoriaPaginados(
                        categoriaId, paginaFinal, tamanoFinal);
                return ResponseEntity.ok(posts);
            } else {
                // Lista general paginada
                PaginatedResponse<PostResponse> posts = postService.obtenerTodosPostsPaginados(
                        paginaFinal, tamanoFinal);
                return ResponseEntity.ok(posts);
            }
        } else {
            // Sin paginación - usar método original (COMPATIBILIDAD)
            List<PostResponse> posts = postService.obtenerTodosPosts();
            return ResponseEntity.ok(posts);
        }
    }

    // Obtener posts por usuario - CON PAGINACIÓN OPCIONAL
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerPostsPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano) {

        // Si vienen parámetros de paginación, usar versión paginada
        if (pagina != null || tamano != null) {
            int paginaFinal = pagina != null ? pagina : 0;
            int tamanoFinal = tamano != null ? tamano : 10;

            PaginatedResponse<PostResponse> posts = postService.obtenerPostsPorUsuarioPaginados(
                    usuarioId, paginaFinal, tamanoFinal);
            return ResponseEntity.ok(posts);
        } else {
            // Sin paginación - método original
            List<PostResponse> posts = postService.obtenerPostsPorUsuario(usuarioId);
            return ResponseEntity.ok(posts);
        }
    }

    // Obtener posts por categoría - CON PAGINACIÓN OPCIONAL
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<?> obtenerPostsPorCategoria(
            @PathVariable Long categoriaId,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano) {

        // Si vienen parámetros de paginación, usar versión paginada
        if (pagina != null || tamano != null) {
            int paginaFinal = pagina != null ? pagina : 0;
            int tamanoFinal = tamano != null ? tamano : 10;

            PaginatedResponse<PostResponse> posts = postService.obtenerPostsPorCategoriaPaginados(
                    categoriaId, paginaFinal, tamanoFinal);
            return ResponseEntity.ok(posts);
        } else {
            // Sin paginación - método original
            List<PostResponse> posts = postService.obtenerPostsPorCategoria(categoriaId);
            return ResponseEntity.ok(posts);
        }
    }

    //Posts más populares (ordenados por likes) - SOLO PAGINADO
    @GetMapping("/populares")
    public ResponseEntity<PaginatedResponse<PostResponse>> obtenerPostsPopulares(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        PaginatedResponse<PostResponse> posts = postService.obtenerPostsPopularesPaginados(pagina, tamano);
        return ResponseEntity.ok(posts);
    }

    //Buscar posts - SOLO PAGINADO
    @GetMapping("/buscar")
    public ResponseEntity<PaginatedResponse<PostResponse>> buscarPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        if (q == null || q.trim().isEmpty()) {
            throw new RuntimeException("El parámetro de búsqueda 'q' es requerido");
        }

        PaginatedResponse<PostResponse> posts = postService.buscarPostsPaginados(q.trim(), pagina, tamano);
        return ResponseEntity.ok(posts);
    }
}