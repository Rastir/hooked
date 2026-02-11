package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarPerfilRequest;
import com.flaco.hooked.domain.response.*;
import com.flaco.hooked.domain.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuarioController {

    @Autowired private UsuarioService usuarioService;

    // ========== PERFIL PROPIO ==========

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponse> miPerfil(Authentication auth) {
        UsuarioResponse usuario = usuarioService.obtenerPerfilPorEmail(auth.getName());

        return ResponseEntity.ok()
                .headers(createProfileHeaders(usuario, "self"))
                .body(usuario);
    }

    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(
            @Valid @RequestBody ActualizarPerfilRequest request,
            Authentication auth) {

        UsuarioResponse actualizado = usuarioService.actualizarPerfil(auth.getName(), request);

        return ResponseEntity.ok()
                .headers(createProfileHeaders(actualizado, "updated"))
                .header("X-Updated-At", LocalDateTime.now().toString())
                .body(actualizado);
    }

    @PostMapping("/perfil/foto")
    public ResponseEntity<Map<String, String>> subirFoto(
            @RequestParam("foto") MultipartFile archivo,
            Authentication auth) {

        validarImagen(archivo);

        String fotoUrl = usuarioService.subirFotoPerfil(auth.getName(), archivo);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-Upload-Success", "true")
                .header("X-File-Size", String.valueOf(archivo.getSize()))
                .body(Map.of(
                        "fotoUrl", fotoUrl,
                        "mensaje", "Foto actualizada exitosamente"
                ));
    }

    // ========== PERFIL PÚBLICO ==========

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> perfilPublico(@PathVariable @Positive Long id) {
        UsuarioResponse usuario = usuarioService.obtenerPerfilPublico(id);

        return ResponseEntity.ok()
                .headers(createProfileHeaders(usuario, "public"))
                .body(usuario);
    }

    // ========== BÚSQUEDAS PAGINADAS ==========

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String buscar,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {

        if (buscar != null && !buscar.trim().isEmpty()) {
            PaginatedResponse<UsuarioResponse> resultados =
                    usuarioService.buscarUsuariosPaginados(buscar.trim(), pagina, tamano);
            return ResponseEntity.ok()
                    .headers(createPaginationHeaders(resultados, "search"))
                    .body(resultados);
        }

        PaginatedResponse<UsuarioResponse> usuarios =
                usuarioService.obtenerUsuariosPaginados(pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(usuarios, "list"))
                .body(usuarios);
    }

    @GetMapping("/especialidad/{tag}")
    public ResponseEntity<PaginatedResponse<UsuarioResponse>> porTag(
            @PathVariable @Size(min = 2) String tag,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {

        PaginatedResponse<UsuarioResponse> usuarios =
                usuarioService.obtenerUsuariosPorTagPaginados(tag.trim(), pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(usuarios, "tag"))
                .header("X-Search-Tag", tag.trim())
                .body(usuarios);
    }

    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<PaginatedResponse<UsuarioResponse>> porNivel(
            @PathVariable @Pattern(regexp = "Principiante|Intermedio|Experto") String nivel,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {

        PaginatedResponse<UsuarioResponse> usuarios =
                usuarioService.obtenerUsuariosPorNivelPaginados(nivel, pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(usuarios, "level"))
                .header("X-Experience-Level", nivel)
                .body(usuarios);
    }

    @GetMapping("/ubicacion/{ubicacion}")
    public ResponseEntity<PaginatedResponse<UsuarioResponse>> porUbicacion(
            @PathVariable @Size(min = 2) String ubicacion,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {

        PaginatedResponse<UsuarioResponse> usuarios =
                usuarioService.obtenerUsuariosPorUbicacionPaginados(ubicacion.trim(), pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(usuarios, "location"))
                .header("X-Search-Location", ubicacion.trim())
                .body(usuarios);
    }

    @GetMapping("/activos")
    public ResponseEntity<PaginatedResponse<UsuarioResponse>> activos(
            @RequestParam(defaultValue = "30") @Min(1) @Max(365) int dias,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {

        PaginatedResponse<UsuarioResponse> usuarios =
                usuarioService.obtenerUsuariosActivosPaginados(dias, pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(usuarios, "active"))
                .header("X-Activity-Period-Days", String.valueOf(dias))
                .body(usuarios);
    }

    @GetMapping("/mas-activos")
    public ResponseEntity<PaginatedResponse<UsuarioResponse>> masActivos(
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(25) int tamano) {

        PaginatedResponse<UsuarioResponse> usuarios =
                usuarioService.obtenerUsuariosMasActivosPaginados(pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(usuarios, "ranking"))
                .header("X-Sort-Order", "posts-desc")
                .body(usuarios);
    }

    @GetMapping("/nuevos")
    public ResponseEntity<PaginatedResponse<UsuarioResponse>> nuevos(
            @RequestParam(defaultValue = "7") @Min(1) @Max(90) int dias,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {

        PaginatedResponse<UsuarioResponse> usuarios =
                usuarioService.obtenerUsuariosNuevosPaginados(dias, pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(usuarios, "new"))
                .header("X-Registration-Period-Days", String.valueOf(dias))
                .body(usuarios);
    }

    @GetMapping("/buscar-avanzado")
    public ResponseEntity<PaginatedResponse<UsuarioResponse>> busquedaAvanzada(
            @RequestParam @Size(min = 2, max = 50) String q,
            @RequestParam(defaultValue = "0") @PositiveOrZero int pagina,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int tamano) {

        PaginatedResponse<UsuarioResponse> usuarios =
                usuarioService.busquedaAvanzadaPaginada(q.trim(), pagina, tamano);

        return ResponseEntity.ok()
                .headers(createPaginationHeaders(usuarios, "advanced"))
                .header("X-Search-Term", q.trim())
                .body(usuarios);
    }

    // ========== ESTADÍSTICAS ==========

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        Map<String, Object> stats = Map.of(
                "totalUsuarios", usuarioService.contarUsuarios(),
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity.ok()
                .header("X-Stats-Generated", "true")
                .header("Cache-Control", "public, max-age=300")
                .body(stats);
    }

    // ========== MÉTODOS PRIVADOS ==========

    private void validarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Archivo de imagen requerido");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }

        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("La imagen no puede exceder 5MB");
        }
    }

    private HttpHeaders createProfileHeaders(UsuarioResponse u, String type) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Profile-Type", type);
        h.add("X-User-ID", u.getId().toString());
        h.add("X-User-Level", u.getNivelPescador() != null ? u.getNivelPescador() : "Principiante");
        h.add("Cache-Control", "self".equals(type) ? "private, max-age=60" : "public, max-age=300");
        return h;
    }

    private HttpHeaders createPaginationHeaders(PaginatedResponse<?> p, String type) {
        HttpHeaders h = new HttpHeaders();
        h.add("X-Query-Type", type);
        h.add("X-Page-Number", String.valueOf(p.getPaginaActual()));
        h.add("X-Page-Size", String.valueOf(p.getTamanoPagina()));
        h.add("X-Total-Elements", String.valueOf(p.getTotalElementos()));
        h.add("Cache-Control", "public, max-age=120");
        return h;
    }
}