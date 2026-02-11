package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.*;
import com.flaco.hooked.domain.response.*;
import com.flaco.hooked.domain.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Validated
public class PostController {

    @Autowired private PostService postService;

    // ========== CRUD BÁSICO ==========

    @PostMapping
    public ResponseEntity<PostResponse> crearPost(
            @Valid @RequestBody CrearPostRequest request,
            Authentication auth) {

        PostResponse creado = postService.crearPost(request, auth.getName());

        return ResponseEntity.created(URI.create("/api/posts/" + creado.getId()))
                .headers(createPostHeaders(creado, "created"))
                .body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> obtenerPost(
            @PathVariable @Positive Long id) {

        PostResponse post = postService.obtenerPostPorId(id);

        return ResponseEntity.ok()
                .headers(createReadHeaders(post))
                .body(post);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> actualizarPost(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ActualizarPostRequest request,
            Authentication auth) {

        PostResponse actualizado = postService.actualizarPost(id, request, auth.getName());

        return ResponseEntity.ok()
                .headers(createPostHeaders(actualizado, "updated"))
                .body(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPost(
            @PathVariable @Positive Long id,
            Authentication auth) {

        postService.eliminarPost(id, auth.getName());

        return ResponseEntity.noContent()
                .header("X-Post-Deleted", "true")
                .header("X-Deleted-At", LocalDateTime.now().toString())
                .build();
    }

    // ========== LIKES ==========

    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponse> toggleLike(
            @PathVariable @Positive Long id,
            Authentication auth) {

        PostResponse post = postService.darLike(id, auth.getName());

        return ResponseEntity.ok()
                .header("X-Like-Action", "toggled")
                .header("X-Total-Likes", post.getLikeCount().toString())
                .body(post);
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<PostResponse> quitarLike(
            @PathVariable @Positive Long id,
            Authentication auth) {

        PostResponse post = postService.quitarLike(id, auth.getName());

        return ResponseEntity.ok()
                .header("X-Unlike-Success", "true")
                .header("X-Total-Likes", post.getLikeCount().toString())
                .body(post);
    }

    // ========== CONSULTAS SIN PAGINACIÓN (compatibilidad) ==========

    @GetMapping("/lista-completa")
    public ResponseEntity<List<PostResponse>> obtenerTodos() {
        List<PostResponse> posts = postService.obtenerTodosPosts();

        return ResponseEntity.ok()
                .header("X-Total-Posts", String.valueOf(posts.size()))
                .header("Cache-Control", "public, max-age=60")
                .body(posts);
    }

    // ========== CONSULTAS PAGINADAS ==========

    @GetMapping
    public ResponseEntity<PaginatedResponse<PostResponse>> listarPosts(
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int tamano,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) @Size(min = 2, max = 100) String buscar) {

        PaginatedResponse<PostResponse> posts;
        String queryType;

        if (buscar != null && !buscar.trim().isEmpty()) {
            posts = postService.buscarPostsPaginados(buscar.trim(), pagina, tamano);
            queryType = "search";
        } else if (categoriaId != null) {
            posts = postService.obtenerPostsPorCategoriaPaginados(categoriaId, pagina, tamano);
            queryType = "category";
        } else {
            posts = postService.obtenerTodosPostsPaginados(pagina, tamano);
            queryType = "list";
        }

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(posts, queryType, pagina))
                .body(posts);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<PaginatedResponse<PostResponse>> postsPorUsuario(
            @PathVariable @Positive Long usuarioId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int tamano) {

        PaginatedResponse<PostResponse> posts =
                postService.obtenerPostsPorUsuarioPaginados(usuarioId, pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(posts, "user-posts", pagina))
                .body(posts);
    }

    @GetMapping("/mis-posts")
    public ResponseEntity<List<PostResponse>> misPosts(Authentication auth) {
        // Tu PostService tiene este método sin paginar
        List<PostResponse> posts = postService.obtenerPostsPorUsuario(auth.getName());

        return ResponseEntity.ok()
                .header("X-Total-Posts", String.valueOf(posts.size()))
                .header("Cache-Control", "private, max-age=60")
                .body(posts);
    }

    @GetMapping("/populares")
    public ResponseEntity<PaginatedResponse<PostResponse>> postsPopulares(
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {

        PaginatedResponse<PostResponse> posts =
                postService.obtenerPostsPopularesPaginados(pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(posts, "popular", pagina))
                .header("X-Sort-Order", "likes-desc")
                .body(posts);
    }

    // ========== HEADERS REUTILIZABLES ==========

    private HttpHeaders createPostHeaders(PostResponse post, String action) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Post-" + action, "true");
        h.add("X-Post-ID", post.getId().toString());
        h.add("X-Author-ID", post.getAutor().getId().toString());
        h.add("Cache-Control", "no-cache");
        return h;
    }

    private HttpHeaders createReadHeaders(PostResponse post) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Post-ID", post.getId().toString());
        h.add("Cache-Control", "public, max-age=300");
        return h;
    }

    private HttpHeaders createPaginationHeaders(
            PaginatedResponse<?> response, String type, int pagina) {

        HttpHeaders h = new HttpHeaders();
        h.add("X-Query-Type", type);
        h.add("X-Page-Number", String.valueOf(pagina));
        h.add("X-Page-Size", String.valueOf(response.getTamanoPagina())); // ← CORREGIDO
        h.add("X-Total-Elements", String.valueOf(response.getTotalElementos()));
        h.add("X-Total-Pages", String.valueOf(response.getTotalPaginas()));
        h.add("Cache-Control", "public, max-age=60");
        return h;
    }
}