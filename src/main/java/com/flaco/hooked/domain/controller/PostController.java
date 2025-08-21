package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarPostRequest;
import com.flaco.hooked.domain.request.CrearPostRequest;
import com.flaco.hooked.domain.response.PostResponse;
import com.flaco.hooked.domain.response.PaginatedResponse;
import com.flaco.hooked.domain.response.MessageResponse;
import com.flaco.hooked.domain.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    // Crear un nuevo post - NIVEL 2
    @PostMapping
    public ResponseEntity<?> crearPost(
            @Valid @RequestBody CrearPostRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            // 🔥 NIVEL 2: Validar Content-Type
            String contentType = httpRequest.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .header("Accept", "application/json")
                        .body(MessageResponse.error("Content-Type debe ser application/json"));
            }

            String emailUsuario = authentication.getName(); // Email del JWT
            PostResponse postCreado = postService.crearPost(request, emailUsuario);

            // 🔥 NIVEL 2: Headers profesionales para creación
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Post-Created", "true");
            headers.add("X-Post-ID", postCreado.getId().toString());
            headers.add("X-Author-ID", postCreado.getAutor().getId().toString());
            headers.add("X-Created-At", LocalDateTime.now().toString());
            headers.add("Cache-Control", "no-cache");

            // ✅ 201 CREATED + Location header optimizado + Headers informativos
            return ResponseEntity.status(HttpStatus.CREATED)
                    .location(URI.create("/api/posts/" + postCreado.getId()))
                    .headers(headers)
                    .body(postCreado);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // 🔥 NIVEL 2: 409 CONFLICT para casos específicos
            if (mensaje.contains("duplicado") || mensaje.contains("ya existe")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .header("X-Conflict-Type", "duplicate-post")
                        .body(MessageResponse.error(mensaje));
            }

            // 🔥 NIVEL 2: 413 PAYLOAD TOO LARGE para contenido muy largo
            if (mensaje.contains("muy largo") || mensaje.contains("excede") || mensaje.contains("límite")) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .header("X-Limit-Exceeded", "content-length")
                        .body(MessageResponse.error(mensaje));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Validation-Error", "invalid-post-data")
                    .body(MessageResponse.error(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al crear el post"));
        }
    }

    // Obtener post por ID - NIVEL 2
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPostPorId(@PathVariable Long id,
                                              HttpServletRequest httpRequest) {
        try {
            // 🔥 NIVEL 2: Validación más específica del ID
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-post-id")
                        .body(MessageResponse.error("ID de post debe ser un número positivo"));
            }

            PostResponse post = postService.obtenerPostPorId(id);

            // 🔥 NIVEL 2: Headers informativos para lectura
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Post-ID", post.getId().toString());
            headers.add("X-Author-ID", post.getAutor().getId().toString());
            headers.add("X-Post-Views", "incremented"); // Si tienes contador de vistas
            headers.add("Cache-Control", "public, max-age=300"); // 5 minutos cache para posts
            headers.add("Last-Modified", post.getFechaCreacion().toString());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(post);

        } catch (RuntimeException e) {
            // ✅ 404 NOT FOUND - Si el post no existe
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("X-Resource-Error", "post-not-found")
                    .header("X-Requested-ID", id.toString())
                    .body(MessageResponse.error("Post no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener el post"));
        }
    }

    // Actualizar post - NIVEL 2
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPost(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPostRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            // 🔥 NIVEL 2: Validar Content-Type
            String contentType = httpRequest.getContentType();
            if (contentType == null || !contentType.contains("application/json")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .header("Accept", "application/json")
                        .body(MessageResponse.error("Content-Type debe ser application/json"));
            }

            // Validación del ID
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-post-id")
                        .body(MessageResponse.error("ID de post inválido"));
            }

            String emailUsuario = authentication.getName();
            PostResponse postActualizado = postService.actualizarPost(id, request, emailUsuario);

            // 🔥 NIVEL 2: Headers para actualización exitosa
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Post-Updated", "true");
            headers.add("X-Post-ID", postActualizado.getId().toString());
            headers.add("X-Updated-At", LocalDateTime.now().toString());
            headers.add("X-Updated-By", postActualizado.getAutor().getId().toString());
            headers.add("Cache-Control", "no-cache");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(postActualizado);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // ✅ 404 NOT FOUND - Si el post no existe
            if (mensaje.contains("no encontrado") || mensaje.contains("no existe")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Resource-Error", "post-not-found")
                        .header("X-Requested-ID", id.toString())
                        .body(MessageResponse.error(mensaje));
            }

            // 🔥 NIVEL 2: 403 FORBIDDEN más específico
            if (mensaje.contains("permisos") || mensaje.contains("autorizado") || mensaje.contains("propietario")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Permission-Error", "not-owner")
                        .header("X-Required-Role", "post-owner")
                        .body(MessageResponse.error("Solo el autor puede actualizar este post"));
            }

            // 🔥 NIVEL 2: 422 UNPROCESSABLE ENTITY para reglas de negocio
            if (mensaje.contains("no se puede") || mensaje.contains("bloqueado") || mensaje.contains("estado")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .header("X-Business-Rule-Error", "post-locked")
                        .body(MessageResponse.error(mensaje));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Validation-Error", "invalid-update-data")
                    .body(MessageResponse.error(mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al actualizar el post"));
        }
    }

    // Eliminar post - NIVEL 2
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPost(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            // Validación del ID
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-post-id")
                        .body(MessageResponse.error("ID de post inválido"));
            }

            String emailUsuario = authentication.getName();
            postService.eliminarPost(id, emailUsuario);

            // 🔥 NIVEL 2: Headers informativos para eliminación
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Post-Deleted", "true");
            headers.add("X-Deleted-ID", id.toString());
            headers.add("X-Deleted-At", LocalDateTime.now().toString());
            headers.add("X-Deleted-By", emailUsuario);

            // ✅ 204 NO CONTENT con headers informativos
            return ResponseEntity.noContent()
                    .headers(headers)
                    .build();

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // ✅ 404 NOT FOUND - Si el post no existe
            if (mensaje.contains("no encontrado") || mensaje.contains("no existe")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Resource-Error", "post-not-found")
                        .header("X-Requested-ID", id.toString())
                        .body(MessageResponse.error(mensaje));
            }

            // 🔥 NIVEL 2: 403 FORBIDDEN específico
            if (mensaje.contains("permisos") || mensaje.contains("autorizado") || mensaje.contains("propietario")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Permission-Error", "not-owner")
                        .header("X-Required-Role", "post-owner-or-admin")
                        .body(MessageResponse.error("Solo el autor puede eliminar este post"));
            }

            // 🔥 NIVEL 2: 422 para posts que no se pueden eliminar
            if (mensaje.contains("no se puede eliminar") || mensaje.contains("tiene comentarios")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .header("X-Business-Rule-Error", "post-has-dependencies")
                        .body(MessageResponse.error(mensaje));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Validation-Error", "deletion-error")
                    .body(MessageResponse.error(mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al eliminar el post"));
        }
    }

    // Incrementar/Decrementar likes de un post (Toggle) - NIVEL 2
    @PostMapping("/{id}/like")
    public ResponseEntity<?> darLike(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        try {
            // 🔥 NIVEL 2: Validar Content-Type (incluso sin body)
            String contentType = httpRequest.getContentType();
            if (contentType != null && !contentType.contains("application/json") && !contentType.contains("application/x-www-form-urlencoded")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .header("Accept", "application/json, application/x-www-form-urlencoded")
                        .body(MessageResponse.error("Content-Type no soportado para esta operación"));
            }

            // Validación del ID
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-post-id")
                        .body(MessageResponse.error("ID de post inválido"));
            }

            String emailUsuario = authentication.getName();
            PostResponse post = postService.darLike(id, emailUsuario);

            // 🔥 NIVEL 2: Headers informativos para likes
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Like-Action", "toggled");
            headers.add("X-Post-ID", post.getId().toString());
            headers.add("X-Total-Likes", post.getLikeCount().toString());
            headers.add("X-Liked-By", emailUsuario);
            headers.add("X-Action-At", LocalDateTime.now().toString());
            headers.add("Cache-Control", "no-cache");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(post);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // ✅ 404 NOT FOUND - Si el post no existe
            if (mensaje.contains("no encontrado") || mensaje.contains("no existe")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Resource-Error", "post-not-found")
                        .header("X-Requested-ID", id.toString())
                        .body(MessageResponse.error(mensaje));
            }

            // 🔥 NIVEL 2: 409 CONFLICT para auto-like (si no está permitido)
            if (mensaje.contains("propio post") || mensaje.contains("auto-like")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .header("X-Business-Rule-Error", "self-like-forbidden")
                        .body(MessageResponse.error("No puedes dar like a tu propio post"));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Like-Error", "like-processing-failed")
                    .body(MessageResponse.error(mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al procesar el like"));
        }
    }

    // Quitar like de un post - NIVEL 2
    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> quitarLike(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            // Validación del ID
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-post-id")
                        .body(MessageResponse.error("ID de post inválido"));
            }

            String emailUsuario = authentication.getName();
            PostResponse post = postService.quitarLike(id, emailUsuario);

            // 🔥 NIVEL 2: Headers para unlike
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Unlike-Success", "true");
            headers.add("X-Post-ID", post.getId().toString());
            headers.add("X-Total-Likes", post.getLikeCount().toString());
            headers.add("X-Unliked-By", emailUsuario);
            headers.add("Cache-Control", "no-cache");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(post);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // ✅ 404 NOT FOUND - Si el post no existe
            if (mensaje.contains("no encontrado") || mensaje.contains("no existe")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Resource-Error", "post-not-found")
                        .header("X-Requested-ID", id.toString())
                        .body(MessageResponse.error(mensaje));
            }

            // 🔥 NIVEL 2: 422 si no había like para quitar
            if (mensaje.contains("no has dado like") || mensaje.contains("no tienes like")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .header("X-Business-Rule-Error", "no-like-to-remove")
                        .body(MessageResponse.error("No has dado like a este post"));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Unlike-Error", "unlike-processing-failed")
                    .body(MessageResponse.error(mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al quitar el like"));
        }
    }

    // Obtener mis posts (del usuario autenticado) - NIVEL 2
    @GetMapping("/mis-posts")
    public ResponseEntity<?> obtenerMisPosts(Authentication authentication) {
        try {
            String emailUsuario = authentication.getName();
            List<PostResponse> posts = postService.obtenerPostsPorUsuario(emailUsuario);

            // 🔥 NIVEL 2: Headers informativos
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-User-Posts", "true");
            headers.add("X-Total-Posts", String.valueOf(posts.size()));
            headers.add("X-User-Email", emailUsuario);
            headers.add("Cache-Control", "private, max-age=60"); // Cache privado 1 minuto

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(posts);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener los posts"));
        }
    }

    // ENDPOINTS CON PAGINACIÓN - NIVEL 2

    // Obtener todos los posts - CON DETECCIÓN AUTOMÁTICA DE PAGINACIÓN - NIVEL 2
    @GetMapping
    public ResponseEntity<?> obtenerPosts(
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String buscar) {
        try {
            // Validaciones de paginación más específicas
            if (pagina != null && pagina < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-number")
                        .body(MessageResponse.error("El número de página no puede ser negativo"));
            }

            if (tamano != null && (tamano <= 0 || tamano > 100)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-size")
                        .header("X-Max-Page-Size", "100")
                        .body(MessageResponse.error("El tamaño de página debe estar entre 1 y 100"));
            }

            // 🔥 NIVEL 2: Headers basados en el tipo de consulta
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "public, max-age=60"); // Cache público 1 minuto

            // Si vienen parámetros de paginación, usar versión paginada
            if (pagina != null || tamano != null) {
                int paginaFinal = pagina != null ? pagina : 0;
                int tamanoFinal = tamano != null ? tamano : 10;

                PaginatedResponse<PostResponse> posts;

                // Determinar qué tipo de consulta hacer
                if (buscar != null && !buscar.trim().isEmpty()) {
                    // Búsqueda paginada
                    posts = postService.buscarPostsPaginados(buscar.trim(), paginaFinal, tamanoFinal);
                    headers.add("X-Query-Type", "search");
                    headers.add("X-Search-Term", buscar.trim());
                } else if (categoriaId != null) {
                    // Filtro por categoría paginado
                    posts = postService.obtenerPostsPorCategoriaPaginados(categoriaId, paginaFinal, tamanoFinal);
                    headers.add("X-Query-Type", "category-filter");
                    headers.add("X-Category-ID", categoriaId.toString());
                } else {
                    // Lista general paginada
                    posts = postService.obtenerTodosPostsPaginados(paginaFinal, tamanoFinal);
                    headers.add("X-Query-Type", "paginated-list");
                }

                // Headers de paginación
                headers.add("X-Page-Number", String.valueOf(paginaFinal));
                headers.add("X-Page-Size", String.valueOf(tamanoFinal));
                headers.add("X-Total-Elements", String.valueOf(posts.getTotalElementos()));
                headers.add("X-Total-Pages", String.valueOf(posts.getTotalPaginas()));

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(posts);
            } else {
                // Sin paginación - usar método original (COMPATIBILIDAD)
                List<PostResponse> posts = postService.obtenerTodosPosts();
                headers.add("X-Query-Type", "full-list");
                headers.add("X-Total-Posts", String.valueOf(posts.size()));

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(posts);
            }

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // 🔥 NIVEL 2: 404 para categorías no encontradas
            if (mensaje.contains("Categoría no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Resource-Error", "category-not-found")
                        .header("X-Requested-Category-ID", categoriaId != null ? categoriaId.toString() : "null")
                        .body(MessageResponse.error(mensaje));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Query-Error", "posts-query-failed")
                    .body(MessageResponse.error(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener los posts"));
        }
    }

    // Obtener posts por usuario - CON PAGINACIÓN OPCIONAL - NIVEL 2
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerPostsPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano) {
        try {
            // Validaciones más específicas
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-user-id")
                        .body(MessageResponse.error("ID de usuario debe ser un número positivo"));
            }

            if (pagina != null && pagina < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-number")
                        .body(MessageResponse.error("El número de página no puede ser negativo"));
            }

            // 🔥 NIVEL 2: Headers informativos
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-User-ID", usuarioId.toString());
            headers.add("Cache-Control", "public, max-age=120"); // Cache 2 minutos para posts de usuario

            // Si vienen parámetros de paginación, usar versión paginada
            if (pagina != null || tamano != null) {
                int paginaFinal = pagina != null ? pagina : 0;
                int tamanoFinal = tamano != null ? tamano : 10;

                PaginatedResponse<PostResponse> posts = postService.obtenerPostsPorUsuarioPaginados(
                        usuarioId, paginaFinal, tamanoFinal);

                // Headers de paginación
                headers.add("X-Query-Type", "user-posts-paginated");
                headers.add("X-Page-Number", String.valueOf(paginaFinal));
                headers.add("X-Page-Size", String.valueOf(tamanoFinal));
                headers.add("X-Total-Elements", String.valueOf(posts.getTotalElementos()));

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(posts);
            } else {
                // Sin paginación - método original
                List<PostResponse> posts = postService.obtenerPostsPorUsuario(usuarioId);
                headers.add("X-Query-Type", "user-posts-full");
                headers.add("X-Total-Posts", String.valueOf(posts.size()));

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(posts);
            }

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // Si el usuario no existe
            if (mensaje.contains("Usuario no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Resource-Error", "user-not-found")
                        .header("X-Requested-User-ID", usuarioId.toString())
                        .body(MessageResponse.error("Usuario no encontrado"));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Query-Error", "user-posts-query-failed")
                    .body(MessageResponse.error(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener los posts del usuario"));
        }
    }

    // Obtener posts por categoría - CON PAGINACIÓN OPCIONAL - NIVEL 2
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<?> obtenerPostsPorCategoria(
            @PathVariable Long categoriaId,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano) {
        try {
            // Validaciones específicas
            if (categoriaId == null || categoriaId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-category-id")
                        .body(MessageResponse.error("ID de categoría debe ser un número positivo"));
            }

            // 🔥 NIVEL 2: Headers informativos
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Category-ID", categoriaId.toString());
            headers.add("Cache-Control", "public, max-age=300"); // Cache 5 minutos para categorías

            // Si vienen parámetros de paginación, usar versión paginada
            if (pagina != null || tamano != null) {
                int paginaFinal = pagina != null ? pagina : 0;
                int tamanoFinal = tamano != null ? tamano : 10;

                PaginatedResponse<PostResponse> posts = postService.obtenerPostsPorCategoriaPaginados(
                        categoriaId, paginaFinal, tamanoFinal);

                // Headers de paginación
                headers.add("X-Query-Type", "category-posts-paginated");
                headers.add("X-Page-Number", String.valueOf(paginaFinal));
                headers.add("X-Total-Elements", String.valueOf(posts.getTotalElementos()));

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(posts);
            } else {
                // Sin paginación - método original
                List<PostResponse> posts = postService.obtenerPostsPorCategoria(categoriaId);
                headers.add("X-Query-Type", "category-posts-full");
                headers.add("X-Total-Posts", String.valueOf(posts.size()));

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(posts);
            }

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // Si la categoría no existe
            if (mensaje.contains("Categoría no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Resource-Error", "category-not-found")
                        .header("X-Requested-Category-ID", categoriaId.toString())
                        .body(MessageResponse.error("Categoría no encontrada"));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Query-Error", "category-posts-query-failed")
                    .body(MessageResponse.error(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener los posts de la categoría"));
        }
    }

    // Posts más populares (ordenados por likes) - SOLO PAGINADO - NIVEL 2
    @GetMapping("/populares")
    public ResponseEntity<?> obtenerPostsPopulares(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        try {
            // Validaciones específicas para posts populares
            if (pagina < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-number")
                        .body(MessageResponse.error("El número de página no puede ser negativo"));
            }

            if (tamano <= 0 || tamano > 50) { // Límite menor para populares
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-size")
                        .header("X-Max-Page-Size", "50")
                        .body(MessageResponse.error("El tamaño de página debe estar entre 1 y 50 para posts populares"));
            }

            PaginatedResponse<PostResponse> posts = postService.obtenerPostsPopularesPaginados(pagina, tamano);

            // 🔥 NIVEL 2: Headers específicos para populares
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Query-Type", "popular-posts");
            headers.add("X-Sort-Order", "likes-desc");
            headers.add("X-Page-Number", String.valueOf(pagina));
            headers.add("X-Total-Elements", String.valueOf(posts.getTotalElementos()));
            headers.add("Cache-Control", "public, max-age=180"); // Cache 3 minutos para populares

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(posts);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener los posts populares"));
        }
    }

    // Buscar posts - SOLO PAGINADO - NIVEL 2
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        try {
            // ✅ 400 BAD REQUEST - Si falta el parámetro de búsqueda
            if (q == null || q.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "missing-search-query")
                        .body(MessageResponse.error("El parámetro de búsqueda 'q' es requerido"));
            }

            // 🔥 NIVEL 2: Validación de longitud de búsqueda
            if (q.trim().length() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "search-query-too-short")
                        .header("X-Min-Query-Length", "2")
                        .body(MessageResponse.error("El término de búsqueda debe tener al menos 2 caracteres"));
            }

            if (q.trim().length() > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "search-query-too-long")
                        .header("X-Max-Query-Length", "100")
                        .body(MessageResponse.error("El término de búsqueda no puede exceder 100 caracteres"));
            }

            // Validaciones de paginación
            if (pagina < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-number")
                        .body(MessageResponse.error("El número de página no puede ser negativo"));
            }

            PaginatedResponse<PostResponse> posts = postService.buscarPostsPaginados(q.trim(), pagina, tamano);

            // 🔥 NIVEL 2: Headers específicos para búsqueda
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Query-Type", "search");
            headers.add("X-Search-Term", q.trim());
            headers.add("X-Search-Results", String.valueOf(posts.getTotalElementos()));
            headers.add("X-Page-Number", String.valueOf(pagina));
            headers.add("X-Results-Found", posts.getTotalElementos() > 0 ? "true" : "false");
            headers.add("Cache-Control", "public, max-age=120"); // Cache 2 minutos para búsquedas

            // 🔥 NIVEL 2: Si no hay resultados, usar 200 pero con header informativo
            if (posts.getTotalElementos() == 0) {
                headers.add("X-No-Results", "true");
                headers.add("X-Suggestion", "try-different-terms");
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(posts);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al buscar los posts"));
        }
    }
}