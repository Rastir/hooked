package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.*;
import com.flaco.hooked.domain.response.*;
import com.flaco.hooked.domain.service.PostService;
import com.flaco.hooked.model.Usuario;
import com.flaco.hooked.domain.repository.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @Autowired private UsuarioRepository usuarioRepository;

    // ========== MÉTODO AUXILIAR PARA EXTRAER USER ID ==========

    /**
     * Extrae el ID numérico del usuario desde el token JWT.
     * Busca el claim "userId" primero, luego intenta parsear "sub" si es numérico.
     * ✅ CORREGIDO: Fallback a búsqueda por email en BD si no se encuentra en JWT.
     */
    private Long extractUserIdFromAuth(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        // Extraer el JWT principal
        if (auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();

            // 1. Intentar obtener userId del claim directamente
            Object userIdClaim = jwt.getClaim("userId");
            if (userIdClaim != null) {
                try {
                    return Long.valueOf(userIdClaim.toString());
                } catch (NumberFormatException e) {
                    // Ignorar y continuar con siguiente opción
                }
            }

            // 2. Intentar parsear el subject como Long (por si acaso)
            String subject = jwt.getSubject();
            try {
                return Long.valueOf(subject);
            } catch (NumberFormatException e) {
                // El subject es un email, no un ID numérico
            }
        }

        // ✅ CORREGIDO: Fallback - buscar en BD por email
        String email = extractEmailFromAuth(auth);
        if (email != null && !email.isEmpty()) {
            try {
                Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
                if (usuario != null) {
                    System.out.println("DEBUG - extractUserIdFromAuth fallback BD: email=" + email + " id=" + usuario.getId());
                    return usuario.getId();
                }
            } catch (Exception e) {
                System.out.println("DEBUG - Error buscando usuario por email: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Extrae el email del usuario desde el token JWT.
     */
    private String extractEmailFromAuth(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return auth.getName(); // Esto devuelve el "sub" del JWT (email)
    }

    // ========== CRUD BÁSICO ==========

    @PostMapping
    public ResponseEntity<PostResponse> crearPost(
            @Valid @RequestBody CrearPostRequest request,
            Authentication auth) {

        String email = extractEmailFromAuth(auth);
        PostResponse creado = postService.crearPost(request, email);

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

        String email = extractEmailFromAuth(auth);
        PostResponse actualizado = postService.actualizarPost(id, request, email);

        return ResponseEntity.ok()
                .headers(createPostHeaders(actualizado, "updated"))
                .body(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPost(
            @PathVariable @Positive Long id,
            Authentication auth) {

        String email = extractEmailFromAuth(auth);
        postService.eliminarPost(id, email);

        return ResponseEntity.noContent()
                .header("X-Post-Deleted", "true")
                .header("X-Deleted-At", LocalDateTime.now().toString())
                .build();
    }

    // ========== LIKES CON TOGGLE ==========

    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponse> toggleLike(
            @PathVariable @Positive Long id,
            Authentication auth) {

        // Usar ID numérico para likes (más eficiente y preciso)
        Long userId = extractUserIdFromAuth(auth);
        String email = extractEmailFromAuth(auth);

        System.out.println("DEBUG - toggleLike controller: userId=" + userId + " email=" + email);

        // El método toggleLike ahora acepta ambos: userId preferido, email como fallback
        PostResponse post = postService.toggleLike(id, userId, email);

        String action = post.getLikedByCurrentUser() ? "liked" : "unliked";

        return ResponseEntity.ok()
                .header("X-Like-Action", action)
                .header("X-Total-Likes", post.getLikeCount().toString())
                .header("X-User-Liked", post.getLikedByCurrentUser().toString())
                .body(post);
    }

    // Mantener DELETE por compatibilidad (opcional)
    @DeleteMapping("/{id}/like")
    public ResponseEntity<PostResponse> quitarLike(
            @PathVariable @Positive Long id,
            Authentication auth) {

        Long userId = extractUserIdFromAuth(auth);
        String email = extractEmailFromAuth(auth);
        PostResponse post = postService.quitarLike(id, userId, email);

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
            @RequestParam(required = false) @Size(min = 2, max = 100) String buscar,
            Authentication auth) {

        PaginatedResponse<PostResponse> posts;
        String queryType;

        // ✅ CORREGIDO: Usar ID numérico para calcular likedByCurrentUser correctamente
        Long userId = extractUserIdFromAuth(auth);

        System.out.println("DEBUG - listarPosts: userId extraído=" + userId + " email=" + extractEmailFromAuth(auth));

        if (buscar != null && !buscar.trim().isEmpty()) {
            posts = postService.buscarPostsPaginados(buscar.trim(), pagina, tamano, userId);
            queryType = "search";
        } else if (categoriaId != null) {
            posts = postService.obtenerPostsPorCategoriaPaginados(categoriaId, pagina, tamano, userId);
            queryType = "category";
        } else {
            posts = postService.obtenerTodosPostsPaginados(pagina, tamano, userId);
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
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int tamano,
            Authentication auth) {

        Long userId = extractUserIdFromAuth(auth);

        PaginatedResponse<PostResponse> posts =
                postService.obtenerPostsPorUsuarioPaginados(usuarioId, pagina, tamano, userId);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(posts, "user-posts", pagina))
                .body(posts);
    }

    @GetMapping("/mis-posts")
    public ResponseEntity<List<PostResponse>> misPosts(Authentication auth) {
        String email = extractEmailFromAuth(auth);
        // Tu PostService tiene este método sin paginar
        List<PostResponse> posts = postService.obtenerPostsPorUsuario(email);

        return ResponseEntity.ok()
                .header("X-Total-Posts", String.valueOf(posts.size()))
                .header("Cache-Control", "no-store, no-cache, must-revalidate")
                .body(posts);
    }

    @GetMapping("/populares")
    public ResponseEntity<PaginatedResponse<PostResponse>> postsPopulares(
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano,
            Authentication auth) {

        Long userId = extractUserIdFromAuth(auth);

        PaginatedResponse<PostResponse> posts =
                postService.obtenerPostsPopularesPaginados(pagina, tamano, userId);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(posts, "popular", pagina))
                .header("X-Sort-Order", "likes-desc")
                .body(posts);
    }

    // ========== HEADERS REUTILIZABLES ==========

    private HttpHeaders createPostHeaders(PostResponse response, String action) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Post-" + action, "true");
        h.add("X-Post-ID", response.getId().toString());
        h.add("X-Author-ID", response.getAutor().getId().toString());
        h.add("Cache-Control", "no-cache");
        return h;
    }

    private HttpHeaders createReadHeaders(PostResponse response) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Post-ID", response.getId().toString());
        h.add("Cache-Control", "no-cache, no-store, must-revalidate");
        h.add("Pragma", "no-cache");
        return h;
    }

    private HttpHeaders createPaginationHeaders(
            PaginatedResponse<?> response, String type, int pagina) {

        HttpHeaders h = new HttpHeaders();
        h.add("X-Query-Type", type);
        h.add("X-Page-Number", String.valueOf(pagina));
        h.add("X-Page-Size", String.valueOf(response.getTamanoPagina()));
        h.add("X-Total-Elements", String.valueOf(response.getTotalElementos()));
        h.add("X-Total-Pages", String.valueOf(response.getTotalPaginas()));
        h.add("Cache-Control", "no-store, no-cache, must-revalidate");
        return h;
    }
}