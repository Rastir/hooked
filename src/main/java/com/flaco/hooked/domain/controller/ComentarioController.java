package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarComentarioRequest;
import com.flaco.hooked.domain.request.CrearComentarioRequest;
import com.flaco.hooked.domain.response.ComentarioResponse;
import com.flaco.hooked.domain.response.PaginatedResponse;
import com.flaco.hooked.domain.response.MessageResponse;
import com.flaco.hooked.domain.service.ComentarioService;
import com.flaco.hooked.model.Usuario;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comentarios")
@CrossOrigin(origins = "http://localhost:3000")
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

    // ===============================
    // 💬 CREAR COMENTARIO - NIVEL 2
    // ===============================
    @PostMapping
    public ResponseEntity<?> crearComentario(
            @Valid @RequestBody CrearComentarioRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        // 🔍 VALIDACIÓN CONTENT-TYPE NIVEL 2
        String contentType = httpRequest.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .header("X-Error-Type", "invalid-content-type")
                    .header("X-Expected-Content-Type", "application/json")
                    .header("X-Received-Content-Type", contentType != null ? contentType : "none")
                    .header("X-Content-Suggestion", "Asegúrese de enviar Content-Type: application/json")
                    .body(Map.of(
                            "error", "Tipo de contenido no soportado",
                            "mensaje", "Este endpoint requiere Content-Type: application/json",
                            "contentTypeEsperado", "application/json",
                            "contentTypeRecibido", contentType != null ? contentType : "none"
                    ));
        }

        // 🔐 VALIDACIÓN AUTHENTICATION NIVEL 2
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Bearer")
                    .header("X-Error-Type", "authentication-required")
                    .header("X-Auth-Suggestion", "Inicie sesión para crear comentarios")
                    .header("X-Login-Endpoint", "/api/auth/login")
                    .body(Map.of(
                            "error", "Autenticación requerida",
                            "mensaje", "Debe iniciar sesión para crear comentarios",
                            "accion", "Proporcione un token JWT válido en el header Authorization"
                    ));
        }

        // 🔍 VALIDACIONES AVANZADAS DE CONTENIDO
        if (request.getContenido() != null) {
            String contenido = request.getContenido().trim();

            if (contenido.length() < 3) {
                return ResponseEntity.badRequest()
                        .header("X-Error-Type", "content-too-short")
                        .header("X-Content-Min-Length", "3")
                        .header("X-Content-Current-Length", String.valueOf(contenido.length()))
                        .body(Map.of(
                                "error", "Contenido muy corto",
                                "mensaje", "El comentario debe tener al menos 3 caracteres",
                                "longitudMinima", 3,
                                "longitudActual", contenido.length()
                        ));
            }

            if (contenido.length() > 1000) {
                return ResponseEntity.badRequest()
                        .header("X-Error-Type", "content-too-long")
                        .header("X-Content-Max-Length", "1000")
                        .header("X-Content-Current-Length", String.valueOf(contenido.length()))
                        .body(Map.of(
                                "error", "Contenido muy largo",
                                "mensaje", "El comentario no puede tener más de 1000 caracteres",
                                "longitudMaxima", 1000,
                                "longitudActual", contenido.length()
                        ));
            }
        }

        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ComentarioResponse comentario = comentarioService.crearComentario(request, usuario);

            // 🎯 HEADERS PROFESIONALES NIVEL 2
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("/api/comentarios/" + comentario.getId()));

            headers.set("X-Comment-Created", "true");
            headers.set("X-Comment-ID", comentario.getId().toString());
            headers.set("X-Author-ID", comentario.getAutor().getId().toString());
            headers.set("X-Author-Name", comentario.getAutor().getNombre());
            headers.set("X-Created-At", LocalDateTime.now().toString());

            if (comentario.getComentarioPadreId() != null) {
                headers.set("X-Comment-Type", "reply");
                headers.set("X-Parent-Comment-ID", comentario.getComentarioPadreId().toString());
                headers.set("X-Nesting-Level", "2");
            } else {
                headers.set("X-Comment-Type", "comment");
                headers.set("X-Nesting-Level", "1");
            }

            headers.set("Cache-Control", "no-cache");
            headers.set("X-Cache-Action", "invalidate-post-comments");

            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(comentario);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            if (mensaje.contains("Post no encontrado") || mensaje.contains("no existe")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Type", "post-not-found")
                        .header("X-Resource-Type", "post")
                        .body(Map.of(
                                "error", "Post no encontrado",
                                "mensaje", "No se puede comentar en un post que no existe"
                        ));
            }

            if (mensaje.contains("Comentario padre no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Type", "parent-comment-not-found")
                        .header("X-Resource-Type", "comment")
                        .body(Map.of(
                                "error", "Comentario padre no encontrado",
                                "mensaje", "No se puede responder a un comentario que no existe"
                        ));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "validation-error")
                    .body(Map.of("error", "Error de validación", "mensaje", mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "comment-creation-failed")
                    .body(Map.of("error", "Error interno", "mensaje", "Error al crear el comentario"));
        }
    }

    // ===============================
    // 👁️ OBTENER COMENTARIO - NIVEL 2
    // ===============================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerComentario(@PathVariable Long id, HttpServletRequest request) {

        if (id == null || id <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-comment-id")
                    .header("X-ID-Format", "positive-integer")
                    .body(Map.of(
                            "error", "ID de comentario inválido",
                            "mensaje", "El ID del comentario debe ser un número positivo"
                    ));
        }

        try {
            ComentarioResponse comentario = comentarioService.obtenerComentario(id);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Comment-ID", comentario.getId().toString());
            headers.set("X-Author-ID", comentario.getAutor().getId().toString());
            headers.set("X-Author-Name", comentario.getAutor().getNombre());
            headers.set("X-Content-Length", String.valueOf(comentario.getContenido().length()));

            if (comentario.getComentarioPadreId() != null) {
                headers.set("X-Comment-Type", "reply");
                headers.set("X-Parent-Comment-ID", comentario.getComentarioPadreId().toString());
                headers.set("X-Nesting-Level", "2");
            } else {
                headers.set("X-Comment-Type", "comment");
                headers.set("X-Nesting-Level", "1");
            }

            headers.set("Cache-Control", "public, max-age=300");
            headers.set("X-Cache-Strategy", "comment-stable");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(comentario);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("X-Error-Type", "comment-not-found")
                    .header("X-Resource-Type", "comment")
                    .body(Map.of(
                            "error", "Comentario no encontrado",
                            "mensaje", "No existe un comentario con el ID especificado"
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "comment-fetch-failed")
                    .body(Map.of("error", "Error interno", "mensaje", "Error al obtener el comentario"));
        }
    }

    // ===============================
    // ✏️ ACTUALIZAR COMENTARIO - NIVEL 2
    // ===============================
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarComentario(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarComentarioRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        String contentType = httpRequest.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .header("X-Error-Type", "invalid-content-type")
                    .header("X-Expected-Content-Type", "application/json")
                    .body(Map.of(
                            "error", "Tipo de contenido no soportado",
                            "mensaje", "Este endpoint requiere Content-Type: application/json"
                    ));
        }

        if (id == null || id <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-comment-id")
                    .body(Map.of(
                            "error", "ID de comentario inválido",
                            "mensaje", "El ID del comentario debe ser un número positivo"
                    ));
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Bearer")
                    .header("X-Error-Type", "authentication-required")
                    .body(Map.of(
                            "error", "Autenticación requerida",
                            "mensaje", "Debe iniciar sesión para editar comentarios"
                    ));
        }

        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            ComentarioResponse comentario = comentarioService.actualizarComentario(id, request, usuario);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Comment-Updated", "true");
            headers.set("X-Comment-ID", comentario.getId().toString());
            headers.set("X-Author-ID", comentario.getAutor().getId().toString());
            headers.set("X-Updated-At", LocalDateTime.now().toString());
            headers.set("Cache-Control", "no-cache");
            headers.set("X-Cache-Action", "invalidate-comment");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(comentario);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            if (mensaje.contains("no encontrado") || mensaje.contains("no existe")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Type", "comment-not-found")
                        .body(Map.of("error", "Comentario no encontrado", "mensaje", mensaje));
            }

            if (mensaje.contains("permisos") || mensaje.contains("autorizado") || mensaje.contains("propietario")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Error-Type", "insufficient-permissions")
                        .body(Map.of("error", "Permisos insuficientes", "mensaje", mensaje));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "validation-error")
                    .body(Map.of("error", "Error de validación", "mensaje", mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "comment-update-failed")
                    .body(Map.of("error", "Error interno", "mensaje", "Error al actualizar el comentario"));
        }
    }

    // ===============================
    // 🗑️ ELIMINAR COMENTARIO - NIVEL 2
    // ===============================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarComentario(@PathVariable Long id, Authentication authentication) {
        if (id == null || id <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-comment-id")
                    .body(Map.of(
                            "error", "ID de comentario inválido",
                            "mensaje", "El ID del comentario debe ser un número positivo"
                    ));
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Bearer")
                    .header("X-Error-Type", "authentication-required")
                    .body(Map.of(
                            "error", "Autenticación requerida",
                            "mensaje", "Debe iniciar sesión para eliminar comentarios"
                    ));
        }

        try {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            comentarioService.eliminarComentario(id, usuario);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Comment-Deleted", "true");
            headers.set("X-Comment-ID", id.toString());
            headers.set("X-Deleted-At", LocalDateTime.now().toString());
            headers.set("Cache-Control", "no-cache");
            headers.set("Clear-Site-Data", "\"cache\"");

            return ResponseEntity.noContent()
                    .headers(headers)
                    .build();

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            if (mensaje.contains("no encontrado") || mensaje.contains("no existe")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Type", "comment-not-found")
                        .body(Map.of("error", "Comentario no encontrado", "mensaje", mensaje));
            }

            if (mensaje.contains("permisos") || mensaje.contains("autorizado") || mensaje.contains("propietario")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .header("X-Error-Type", "insufficient-permissions")
                        .body(Map.of("error", "Permisos insuficientes", "mensaje", mensaje));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "deletion-error")
                    .body(Map.of("error", "Error al eliminar", "mensaje", mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "comment-deletion-failed")
                    .body(Map.of("error", "Error interno", "mensaje", "Error al eliminar el comentario"));
        }
    }

    // ===============================
    // 📝 OBTENER COMENTARIOS POR POST - NIVEL 2 CORREGIDO
    // ===============================
    @GetMapping("/post/{postId}")
    public ResponseEntity<?> obtenerComentariosPorPost(
            @PathVariable Long postId,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano,
            @RequestParam(required = false) String tipo) {

        if (postId == null || postId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-post-id")
                    .body(Map.of(
                            "error", "ID de post inválido",
                            "mensaje", "El ID del post debe ser un número positivo"
                    ));
        }

        if (pagina != null && pagina < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-pagination")
                    .body(Map.of(
                            "error", "Paginación inválida",
                            "mensaje", "El número de página no puede ser negativo"
                    ));
        }

        if (tamano != null && (tamano <= 0 || tamano > 100)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "pagination-limit-exceeded")
                    .body(Map.of(
                            "error", "Límite de paginación excedido",
                            "mensaje", "El tamaño de página debe estar entre 1 y 100"
                    ));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Post-ID", postId.toString());

            if (pagina != null || tamano != null) {
                int paginaFinal = pagina != null ? pagina : 0;
                int tamanoFinal = tamano != null ? tamano : 20;

                PaginatedResponse<ComentarioResponse> comentarios;

                if ("principales".equals(tipo)) {
                    comentarios = comentarioService.obtenerComentariosPrincipalesPorPostPaginados(postId, paginaFinal, tamanoFinal);
                    headers.set("X-Query-Type", "main-comments-paginated");
                } else {
                    comentarios = comentarioService.obtenerComentariosPorPostPaginados(postId, paginaFinal, tamanoFinal);
                    headers.set("X-Query-Type", "all-comments-paginated");
                }

                // 🎯 HEADERS CORREGIDOS PARA TU PaginatedResponse
                headers.set("X-Page-Number", String.valueOf(comentarios.getPaginaActual()));
                headers.set("X-Page-Size", String.valueOf(comentarios.getTamanoPagina()));
                headers.set("X-Total-Elements", String.valueOf(comentarios.getTotalElementos()));
                headers.set("X-Total-Pages", String.valueOf(comentarios.getTotalPaginas()));
                headers.set("X-Is-Last-Page", String.valueOf(comentarios.isEsUltimaPagina()));
                headers.set("X-Is-First-Page", String.valueOf(comentarios.isEsPrimeraPagina()));
                headers.set("X-Is-Empty", String.valueOf(comentarios.isEstaVacia()));

                // Calculamos has-next y has-previous basándonos en tu estructura
                boolean hasNext = !comentarios.isEsUltimaPagina();
                boolean hasPrevious = !comentarios.isEsPrimeraPagina();
                headers.set("X-Has-Next-Page", String.valueOf(hasNext));
                headers.set("X-Has-Previous-Page", String.valueOf(hasPrevious));

                if (comentarios.isEstaVacia()) {
                    headers.set("X-No-Comments", "true");
                    headers.set("X-Comments-Suggestion", "¡Sé el primero en comentar este post!");
                } else {
                    headers.set("X-Comments-Found", "true");
                    headers.set("X-Comments-In-Page", String.valueOf(comentarios.getContenido().size()));
                }

                headers.set("Cache-Control", "public, max-age=180");
                headers.set("X-Cache-Strategy", "comments-paginated");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(comentarios);

            } else {
                // Sin paginación - método original
                List<ComentarioResponse> comentarios = comentarioService.obtenerComentariosPorPost(postId);

                headers.set("X-Query-Type", "all-comments-list");
                headers.set("X-Comments-Count", String.valueOf(comentarios.size()));
                headers.set("X-Pagination-Type", "none");

                if (comentarios.isEmpty()) {
                    headers.set("X-No-Comments", "true");
                    headers.set("X-Comments-Suggestion", "¡Sé el primero en comentar!");
                } else {
                    headers.set("X-Comments-Found", "true");
                }

                headers.set("Cache-Control", "public, max-age=240");
                headers.set("X-Cache-Strategy", "comments-full-list");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(comentarios);
            }

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            if (mensaje.contains("Post no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Type", "post-not-found")
                        .body(Map.of(
                                "error", "Post no encontrado",
                                "mensaje", "No se pueden obtener comentarios de un post que no existe"
                        ));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "comments-fetch-error")
                    .body(Map.of("error", "Error al obtener comentarios", "mensaje", mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "comments-fetch-failed")
                    .body(Map.of("error", "Error interno", "mensaje", "Error al obtener comentarios del post"));
        }
    }

    // ===============================
    // 👤 OBTENER COMENTARIOS POR USUARIO - NIVEL 2 CORREGIDO
    // ===============================
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> obtenerComentariosPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano,
            @RequestParam(required = false) String tipo) {

        if (usuarioId == null || usuarioId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-user-id")
                    .body(Map.of(
                            "error", "ID de usuario inválido",
                            "mensaje", "El ID del usuario debe ser un número positivo"
                    ));
        }

        if (pagina != null && pagina < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-pagination")
                    .body(Map.of(
                            "error", "Paginación inválida",
                            "mensaje", "El número de página no puede ser negativo"
                    ));
        }

        if (tamano != null && tamano > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "user-comments-limit-exceeded")
                    .body(Map.of(
                            "error", "Límite excedido para comentarios de usuario",
                            "mensaje", "Máximo 50 comentarios por página para perfiles"
                    ));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-ID", usuarioId.toString());

            if (pagina != null || tamano != null) {
                int paginaFinal = pagina != null ? pagina : 0;
                int tamanoFinal = tamano != null ? tamano : 20;

                PaginatedResponse<ComentarioResponse> comentarios;

                if ("recientes".equals(tipo)) {
                    comentarios = comentarioService.obtenerComentariosRecientesPorUsuarioPaginados(usuarioId, paginaFinal, tamanoFinal);
                    headers.set("X-Query-Type", "user-recent-comments");
                } else {
                    comentarios = comentarioService.obtenerComentariosPorUsuarioPaginados(usuarioId, paginaFinal, tamanoFinal);
                    headers.set("X-Query-Type", "user-all-comments");
                }

                // Headers corregidos para tu PaginatedResponse
                headers.set("X-Page-Number", String.valueOf(comentarios.getPaginaActual()));
                headers.set("X-Page-Size", String.valueOf(comentarios.getTamanoPagina()));
                headers.set("X-Total-Elements", String.valueOf(comentarios.getTotalElementos()));
                headers.set("X-Total-Pages", String.valueOf(comentarios.getTotalPaginas()));

                if (comentarios.getTotalElementos() > 0) {
                    headers.set("X-User-Comments-Found", "true");
                    headers.set("X-User-Activity-Level", clasificarActividadUsuario(comentarios.getTotalElementos()));
                } else {
                    headers.set("X-User-Comments-Found", "false");
                    headers.set("X-User-Activity-Level", "inactive");
                }

                headers.set("Cache-Control", "private, max-age=300");
                headers.set("X-Cache-Strategy", "user-profile-comments");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(comentarios);

            } else {
                List<ComentarioResponse> comentarios = comentarioService.obtenerComentariosPorUsuario(usuarioId);

                headers.set("X-Query-Type", "user-comments-list");
                headers.set("X-Comments-Count", String.valueOf(comentarios.size()));

                if (comentarios.isEmpty()) {
                    headers.set("X-User-Activity", "no-comments");
                } else {
                    headers.set("X-User-Activity", "has-comments");
                }

                headers.set("Cache-Control", "private, max-age=360");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(comentarios);
            }

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            if (mensaje.contains("Usuario no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Type", "user-not-found")
                        .body(Map.of(
                                "error", "Usuario no encontrado",
                                "mensaje", "No se pueden obtener comentarios de un usuario que no existe"
                        ));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "user-comments-fetch-error")
                    .body(Map.of("error", "Error al obtener comentarios del usuario", "mensaje", mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "user-comments-fetch-failed")
                    .body(Map.of("error", "Error interno", "mensaje", "Error al obtener comentarios del usuario"));
        }
    }

    // ===============================
    // 🎯 OBTENER COMENTARIOS PRINCIPALES - NIVEL 2 CORREGIDO
    // ===============================
    @GetMapping("/post/{postId}/principales")
    public ResponseEntity<?> obtenerComentariosPrincipales(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {

        if (postId == null || postId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-post-id")
                    .body(Map.of(
                            "error", "ID de post inválido",
                            "mensaje", "ID requerido para obtener comentarios principales"
                    ));
        }

        if (pagina < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-pagination")
                    .body(Map.of(
                            "error", "Paginación inválida",
                            "mensaje", "La página debe ser mayor o igual a 0"
                    ));
        }

        if (tamano <= 0 || tamano > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "main-comments-limit-exceeded")
                    .body(Map.of(
                            "error", "Límite de comentarios principales excedido",
                            "mensaje", "Tamaño debe estar entre 1 y 100"
                    ));
        }

        try {
            PaginatedResponse<ComentarioResponse> comentarios =
                    comentarioService.obtenerComentariosPrincipalesPorPostPaginados(postId, pagina, tamano);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Post-ID", postId.toString());
            headers.set("X-Query-Type", "main-comments-only");
            headers.set("X-Comment-Level", "1");

            // Headers corregidos para tu PaginatedResponse
            headers.set("X-Page-Number", String.valueOf(comentarios.getPaginaActual()));
            headers.set("X-Page-Size", String.valueOf(comentarios.getTamanoPagina()));
            headers.set("X-Total-Main-Comments", String.valueOf(comentarios.getTotalElementos()));
            headers.set("X-Total-Pages", String.valueOf(comentarios.getTotalPaginas()));

            if (comentarios.isEstaVacia()) {
                headers.set("X-No-Main-Comments", "true");
                headers.set("X-Suggestion", "¡Inicia la conversación con el primer comentario!");
            } else {
                headers.set("X-Main-Comments-Found", "true");
                headers.set("X-Conversation-Starters", String.valueOf(comentarios.getContenido().size()));
            }

            headers.set("Cache-Control", "public, max-age=300");
            headers.set("X-Cache-Strategy", "main-comments-stable");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(comentarios);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            if (mensaje.contains("Post no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Type", "post-not-found")
                        .body(Map.of(
                                "error", "Post no encontrado",
                                "mensaje", "No se pueden obtener comentarios principales de un post inexistente"
                        ));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "main-comments-fetch-error")
                    .body(Map.of("error", "Error al obtener comentarios principales", "mensaje", mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "main-comments-fetch-failed")
                    .body(Map.of("error", "Error interno", "mensaje", "Error al obtener comentarios principales"));
        }
    }

    // ===============================
    // 💬 OBTENER RESPUESTAS - NIVEL 2 CORREGIDO
    // ===============================
    @GetMapping("/{comentarioId}/respuestas")
    public ResponseEntity<?> obtenerRespuestas(
            @PathVariable Long comentarioId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        if (comentarioId == null || comentarioId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-comment-id")
                    .body(Map.of(
                            "error", "ID de comentario inválido",
                            "mensaje", "ID requerido para obtener respuestas del comentario"
                    ));
        }

        if (pagina < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-pagination")
                    .body(Map.of(
                            "error", "Paginación inválida para respuestas",
                            "mensaje", "La página debe ser mayor o igual a 0"
                    ));
        }

        if (tamano <= 0 || tamano > 50) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "replies-limit-exceeded")
                    .body(Map.of(
                            "error", "Límite de respuestas excedido",
                            "mensaje", "Máximo 50 respuestas por página"
                    ));
        }

        try {
            PaginatedResponse<ComentarioResponse> respuestas =
                    comentarioService.obtenerRespuestasPaginadas(comentarioId, pagina, tamano);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Parent-Comment-ID", comentarioId.toString());
            headers.set("X-Query-Type", "comment-replies");
            headers.set("X-Nesting-Level", "2");

            // Headers corregidos para tu PaginatedResponse
            headers.set("X-Page-Number", String.valueOf(respuestas.getPaginaActual()));
            headers.set("X-Page-Size", String.valueOf(respuestas.getTamanoPagina()));
            headers.set("X-Total-Replies", String.valueOf(respuestas.getTotalElementos()));
            headers.set("X-Total-Pages", String.valueOf(respuestas.getTotalPaginas()));

            if (respuestas.isEstaVacia()) {
                headers.set("X-No-Replies", "true");
                headers.set("X-Reply-Suggestion", "¡Sé el primero en responder este comentario!");
            } else {
                headers.set("X-Replies-Found", "true");
                headers.set("X-Discussion-Active", "true");
                headers.set("X-Reply-Count", String.valueOf(respuestas.getContenido().size()));
            }

            headers.set("X-Parent-Comment-URL", "/api/comentarios/" + comentarioId);
            headers.set("Cache-Control", "public, max-age=240");
            headers.set("X-Cache-Strategy", "replies-stable");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(respuestas);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            if (mensaje.contains("Comentario no encontrado") || mensaje.contains("padre no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Type", "parent-comment-not-found")
                        .body(Map.of(
                                "error", "Comentario padre no encontrado",
                                "mensaje", "No se pueden obtener respuestas de un comentario que no existe"
                        ));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "replies-fetch-error")
                    .body(Map.of("error", "Error al obtener respuestas", "mensaje", mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "replies-fetch-failed")
                    .body(Map.of("error", "Error interno", "mensaje", "Error al obtener respuestas del comentario"));
        }
    }

    // ===============================
    // 🕒 OBTENER COMENTARIOS RECIENTES - NIVEL 2 CORREGIDO
    // ===============================
    @GetMapping("/usuario/{usuarioId}/recientes")
    public ResponseEntity<?> obtenerComentariosRecientes(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "15") int tamano) {

        if (usuarioId == null || usuarioId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-user-id")
                    .body(Map.of(
                            "error", "ID de usuario inválido",
                            "mensaje", "ID requerido para obtener comentarios recientes"
                    ));
        }

        if (pagina < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "invalid-pagination")
                    .body(Map.of(
                            "error", "Paginación inválida",
                            "mensaje", "La página debe ser mayor o igual a 0"
                    ));
        }

        if (tamano > 25) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "recent-comments-limit-exceeded")
                    .body(Map.of(
                            "error", "Límite de comentarios recientes excedido",
                            "mensaje", "Máximo 25 comentarios recientes para perfiles"
                    ));
        }

        try {
            PaginatedResponse<ComentarioResponse> comentarios =
                    comentarioService.obtenerComentariosRecientesPorUsuarioPaginados(usuarioId, pagina, tamano);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-ID", usuarioId.toString());
            headers.set("X-Query-Type", "user-recent-comments");
            headers.set("X-Time-Filter", "recent");

            // Headers corregidos para tu PaginatedResponse
            headers.set("X-Page-Number", String.valueOf(comentarios.getPaginaActual()));
            headers.set("X-Page-Size", String.valueOf(comentarios.getTamanoPagina()));
            headers.set("X-Total-Recent-Comments", String.valueOf(comentarios.getTotalElementos()));

            if (comentarios.isEstaVacia()) {
                headers.set("X-Recent-Activity", "none");
                headers.set("X-Activity-Status", "inactive-recently");
            } else {
                headers.set("X-Recent-Activity", "active");
                headers.set("X-Activity-Status", "recently-active");
                headers.set("X-Activity-Level", clasificarActividadReciente(comentarios.getContenido().size()));
            }

            headers.set("Cache-Control", "private, max-age=120");
            headers.set("X-Cache-Strategy", "recent-activity-dynamic");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(comentarios);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            if (mensaje.contains("Usuario no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Type", "user-not-found")
                        .body(Map.of(
                                "error", "Usuario no encontrado",
                                "mensaje", "No se pueden obtener comentarios recientes de un usuario inexistente"
                        ));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Error-Type", "recent-comments-fetch-error")
                    .body(Map.of("error", "Error al obtener comentarios recientes", "mensaje", mensaje));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Type", "recent-comments-fetch-failed")
                    .body(Map.of("error", "Error interno", "mensaje", "Error al obtener comentarios recientes"));
        }
    }

    // ===============================
    // 🛠️ MÉTODOS AUXILIARES NIVEL 2
    // ===============================

    private String clasificarActividadUsuario(long totalComentarios) {
        if (totalComentarios == 0) return "inactive";
        if (totalComentarios <= 5) return "low";
        if (totalComentarios <= 20) return "moderate";
        if (totalComentarios <= 50) return "active";
        if (totalComentarios <= 100) return "very-active";
        return "super-active";
    }

    private String clasificarActividadReciente(int comentariosRecientes) {
        if (comentariosRecientes == 0) return "no-recent-activity";
        if (comentariosRecientes <= 3) return "light-activity";
        if (comentariosRecientes <= 8) return "moderate-activity";
        if (comentariosRecientes <= 15) return "high-activity";
        return "very-high-activity";
    }
}