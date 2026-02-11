package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.*;
import com.flaco.hooked.domain.response.*;
import com.flaco.hooked.domain.service.ComentarioService;
import com.flaco.hooked.model.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/comentarios")
@Validated
public class ComentarioController {

    @Autowired private ComentarioService comentarioService;

    // ========== CRUD ==========

    @PostMapping
    public ResponseEntity<ComentarioResponse> crear(
            @Valid @RequestBody CrearComentarioRequest request,
            Authentication auth) {

        Usuario usuario = extractUsuario(auth);
        ComentarioResponse creado = comentarioService.crearComentario(request, usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(createCommentHeaders(creado, "created"))
                .body(creado);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioResponse> obtener(@PathVariable @Positive Long id) {
        ComentarioResponse comentario = comentarioService.obtenerComentario(id);

        return ResponseEntity.ok()
                .headers(createReadHeaders(comentario))
                .body(comentario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComentarioResponse> actualizar(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ActualizarComentarioRequest request,
            Authentication auth) {

        Usuario usuario = extractUsuario(auth);
        ComentarioResponse actualizado = comentarioService.actualizarComentario(id, request, usuario);

        return ResponseEntity.ok()
                .headers(createCommentHeaders(actualizado, "updated"))
                .body(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable @Positive Long id,
            Authentication auth) {

        Usuario usuario = extractUsuario(auth);
        comentarioService.eliminarComentario(id, usuario);

        return ResponseEntity.noContent()
                .header("X-Comment-Deleted", "true")
                .header("X-Deleted-At", LocalDateTime.now().toString())
                .build();
    }

    // ========== CONSULTAS POR POST ==========

    @GetMapping("/post/{postId}")
    public ResponseEntity<?> porPost(
            @PathVariable @Positive Long postId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamano,
            @RequestParam(required = false) String tipo) {

        boolean soloPrincipales = "principales".equals(tipo);

        PaginatedResponse<ComentarioResponse> comentarios = soloPrincipales
                ? comentarioService.obtenerComentariosPrincipalesPorPostPaginados(postId, pagina, tamano)
                : comentarioService.obtenerComentariosPorPostPaginados(postId, pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(comentarios, soloPrincipales ? "main-comments" : "all-comments"))
                .body(comentarios);
    }

    @GetMapping("/post/{postId}/principales")
    public ResponseEntity<PaginatedResponse<ComentarioResponse>> principales(
            @PathVariable @Positive Long postId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamano) {

        PaginatedResponse<ComentarioResponse> comentarios =
                comentarioService.obtenerComentariosPrincipalesPorPostPaginados(postId, pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(comentarios, "main-comments"))
                .body(comentarios);
    }

    // ========== CONSULTAS POR USUARIO ==========

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> porUsuario(
            @PathVariable @Positive Long usuarioId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int tamano,
            @RequestParam(required = false) String tipo) {

        boolean recientes = "recientes".equals(tipo);

        PaginatedResponse<ComentarioResponse> comentarios = recientes
                ? comentarioService.obtenerComentariosRecientesPorUsuarioPaginados(usuarioId, pagina, tamano)
                : comentarioService.obtenerComentariosPorUsuarioPaginados(usuarioId, pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(comentarios, recientes ? "user-recent" : "user-all"))
                .body(comentarios);
    }

    @GetMapping("/usuario/{usuarioId}/recientes")
    public ResponseEntity<PaginatedResponse<ComentarioResponse>> recientes(
            @PathVariable @Positive Long usuarioId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "15") @Min(1) @Max(25) int tamano) {

        PaginatedResponse<ComentarioResponse> comentarios =
                comentarioService.obtenerComentariosRecientesPorUsuarioPaginados(usuarioId, pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(comentarios, "user-recent"))
                .body(comentarios);
    }

    // ========== RESPUESTAS ANIDADAS ==========

    @GetMapping("/{comentarioId}/respuestas")
    public ResponseEntity<PaginatedResponse<ComentarioResponse>> respuestas(
            @PathVariable @Positive Long comentarioId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {

        PaginatedResponse<ComentarioResponse> respuestas =
                comentarioService.obtenerRespuestasPaginadas(comentarioId, pagina, tamano);

        HttpHeaders headers = createPaginationHeaders(respuestas, "replies");
        headers.add("X-Parent-Comment-ID", comentarioId.toString());
        headers.add("X-Nesting-Level", "2");

        return ResponseEntity.ok()
                .headers(headers)
                .body(respuestas);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private Usuario extractUsuario(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Autenticación requerida");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario) {
            return (Usuario) principal;
        }
        throw new RuntimeException("Tipo de usuario no soportado");
    }

    private HttpHeaders createCommentHeaders(ComentarioResponse c, String action) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Comment-" + action, "true");
        h.add("X-Comment-ID", c.getId().toString());
        h.add("X-Author-ID", c.getAutor().getId().toString());
        h.add("Cache-Control", "no-cache");

        if (c.getComentarioPadreId() != null) {
            h.add("X-Comment-Type", "reply");
            h.add("X-Parent-ID", c.getComentarioPadreId().toString());
        } else {
            h.add("X-Comment-Type", "comment");
        }

        return h;
    }

    private HttpHeaders createReadHeaders(ComentarioResponse c) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Comment-ID", c.getId().toString());
        h.add("Cache-Control", "public, max-age=300");
        return h;
    }

    private HttpHeaders createPaginationHeaders(PaginatedResponse<?> p, String type) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Query-Type", type);
        h.add("X-Page-Number", String.valueOf(p.getPaginaActual()));
        h.add("X-Page-Size", String.valueOf(p.getTamanoPagina()));
        h.add("X-Total-Elements", String.valueOf(p.getTotalElementos()));
        h.add("X-Total-Pages", String.valueOf(p.getTotalPaginas()));
        h.add("Cache-Control", "public, max-age=180");
        return h;
    }
}