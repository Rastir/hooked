package com.flaco.hooked.domain.controller;

import com.flaco.hooked.domain.request.ActualizarPerfilRequest;
import com.flaco.hooked.domain.response.UsuarioResponse;
import com.flaco.hooked.domain.response.PaginatedResponse;
import com.flaco.hooked.domain.response.MessageResponse;
import com.flaco.hooked.domain.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // PERFIL DEL USUARIO AUTENTICADO - NIVEL 2
    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerMiPerfil(Authentication authentication,
                                             HttpServletRequest httpRequest) {
        try {
            String email = authentication.getName();
            UsuarioResponse usuario = usuarioService.obtenerPerfilPorEmail(email);

            // 🔥 NIVEL 2: Headers informativos para perfil propio
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Profile-Type", "self");
            headers.add("X-User-ID", usuario.getId().toString());
            headers.add("X-User-Level", usuario.getNivelPescador() != null ? usuario.getNivelPescador() : "Principiante");
            headers.add("X-Total-Posts", usuario.getTotalPosts() != null ? usuario.getTotalPosts().toString() : "0");
            headers.add("Cache-Control", "private, max-age=60"); // Cache privado 1 minuto
            headers.add("Last-Modified", usuario.getUltimaActividad() != null ? usuario.getUltimaActividad().toString() : "");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuario);

        } catch (RuntimeException e) {
            // ✅ 404 NOT FOUND - Si el usuario autenticado no existe (caso raro)
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("X-Profile-Error", "authenticated-user-not-found")
                    .body(MessageResponse.error("Perfil de usuario no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener el perfil"));
        }
    }

    // VER PERFIL PÚBLICO DE OTRO USUARIO - NIVEL 2
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPerfilPublico(@PathVariable Long id,
                                                  HttpServletRequest httpRequest) {
        try {
            // 🔥 NIVEL 2: Validación más robusta del ID
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-user-id")
                        .header("X-ID-Requirements", "positive-integer")
                        .body(MessageResponse.error("ID de usuario debe ser un número positivo"));
            }

            UsuarioResponse usuario = usuarioService.obtenerPerfilPublico(id);

            // 🔥 NIVEL 2: Headers informativos para perfil público
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Profile-Type", "public");
            headers.add("X-User-ID", usuario.getId().toString());
            headers.add("X-User-Level", usuario.getNivelPescador() != null ? usuario.getNivelPescador() : "Principiante");
            headers.add("X-User-Posts", usuario.getTotalPosts() != null ? usuario.getTotalPosts().toString() : "0");
            headers.add("X-User-Likes", usuario.getTotalLikes() != null ? usuario.getTotalLikes().toString() : "0");
            headers.add("X-Profile-Completeness", calcularCompletenessScore(usuario));
            headers.add("Cache-Control", "public, max-age=300"); // Cache público 5 minutos

            // Headers de ubicación si está disponible
            if (usuario.getUbicacionPreferida() != null) {
                headers.add("X-User-Location", usuario.getUbicacionPreferida());
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuario);

        } catch (RuntimeException e) {
            // ✅ 404 NOT FOUND - Usuario no existe
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header("X-Resource-Error", "user-not-found")
                    .header("X-Requested-ID", id.toString())
                    .body(MessageResponse.error("Usuario no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener el perfil del usuario"));
        }
    }

    // ACTUALIZAR MI PERFIL - NIVEL 2
    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarMiPerfil(
            @Valid @RequestBody ActualizarPerfilRequest request,
            BindingResult bindingResult,
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

            // ✅ 400 BAD REQUEST - Errores de validación más detallados
            if (bindingResult.hasErrors()) {
                Map<String, String> errores = new HashMap<>();
                bindingResult.getFieldErrors().forEach(error ->
                        errores.put(error.getField(), error.getDefaultMessage())
                );

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "field-validation-failed")
                        .header("X-Error-Count", String.valueOf(errores.size()))
                        .body(errores);
            }

            String email = authentication.getName();
            UsuarioResponse usuario = usuarioService.actualizarPerfil(email, request);

            // 🔥 NIVEL 2: Headers para actualización exitosa
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Profile-Updated", "true");
            headers.add("X-User-ID", usuario.getId().toString());
            headers.add("X-Updated-At", LocalDateTime.now().toString());
            headers.add("X-Profile-Completeness", calcularCompletenessScore(usuario));
            headers.add("Cache-Control", "private, no-cache");

            // Headers específicos según lo que se actualizó
            if (request.getNombre() != null) {
                headers.add("X-Updated-Fields", headers.getFirst("X-Updated-Fields") + "name,");
            }
            if (request.getEmail() != null) {
                headers.add("X-Updated-Fields", headers.getFirst("X-Updated-Fields") + "email,");
            }
            if (request.getBio() != null) {
                headers.add("X-Updated-Fields", headers.getFirst("X-Updated-Fields") + "bio,");
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuario); // ✅ 200 OK - Perfil actualizado

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // 🔥 NIVEL 2: 409 CONFLICT para email duplicado
            if (mensaje.contains("Email ya está en uso") || mensaje.contains("email ya está registrado")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .header("X-Conflict-Field", "email")
                        .header("X-Conflict-Type", "duplicate-email")
                        .body(MessageResponse.error("El email ya está siendo usado por otro usuario"));
            }

            // 🔥 NIVEL 2: 401 UNAUTHORIZED para contraseña incorrecta
            if (mensaje.contains("Contraseña actual incorrecta")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .header("X-Auth-Error", "invalid-current-password")
                        .body(MessageResponse.error("La contraseña actual es incorrecta"));
            }

            // ✅ 404 NOT FOUND - Si el usuario no existe
            if (mensaje.contains("Usuario no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Resource-Error", "user-not-found")
                        .body(MessageResponse.error(mensaje));
            }

            // 🔥 NIVEL 2: 422 UNPROCESSABLE ENTITY para reglas de negocio
            if (mensaje.contains("Nivel debe ser") || mensaje.contains("no válido")) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .header("X-Business-Rule-Error", "invalid-level")
                        .body(MessageResponse.error(mensaje));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Update-Error", "profile-update-failed")
                    .body(MessageResponse.error(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al actualizar el perfil"));
        }
    }

    // FOTO DE PERFIL - NIVEL 2
    @PostMapping("/perfil/foto")
    public ResponseEntity<?> subirFotoPerfil(
            @RequestParam("foto") MultipartFile archivo,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            // 🔥 NIVEL 2: Validaciones más específicas de archivos
            if (archivo == null || archivo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "missing-file")
                        .header("X-Required-Parameter", "foto")
                        .body(MessageResponse.error("Archivo de imagen requerido"));
            }

            // 🔥 NIVEL 2: Validación de Content-Type específica
            String contentType = archivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .header("X-Media-Error", "not-image")
                        .header("Accept", "image/jpeg, image/png, image/gif, image/webp")
                        .body(MessageResponse.error("El archivo debe ser una imagen (JPEG, PNG, GIF, WebP)"));
            }

            // 🔥 NIVEL 2: Validación de tamaño más específica
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (archivo.getSize() > maxSize) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .header("X-File-Size-Error", "too-large")
                        .header("X-Max-File-Size", "5MB")
                        .header("X-Current-File-Size", String.valueOf(archivo.getSize() / (1024 * 1024)) + "MB")
                        .body(MessageResponse.error("La imagen no puede exceder 5MB"));
            }

            String email = authentication.getName();
            String fotoUrl = usuarioService.subirFotoPerfil(email, archivo);

            Map<String, String> response = new HashMap<>();
            response.put("fotoUrl", fotoUrl);
            response.put("mensaje", "Foto de perfil actualizada exitosamente");
            response.put("tamaño", String.valueOf(archivo.getSize()));
            response.put("tipo", contentType);

            // 🔥 NIVEL 2: Headers específicos para upload
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Upload-Success", "true");
            headers.add("X-File-Size", String.valueOf(archivo.getSize()));
            headers.add("X-File-Type", contentType);
            headers.add("X-Upload-At", LocalDateTime.now().toString());
            headers.add("X-Storage-Service", "cloudinary");
            headers.add("Cache-Control", "no-cache");

            // ✅ 201 CREATED - Imagen subida exitosamente
            return ResponseEntity.status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(response);

        } catch (RuntimeException e) {
            String mensaje = e.getMessage();

            // 🔥 NIVEL 2: Diferentes tipos de errores de archivo
            if (mensaje.contains("debe ser una imagen") || mensaje.contains("tipo de archivo")) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                        .header("X-Media-Error", "invalid-image-format")
                        .body(MessageResponse.error(mensaje));
            }

            if (mensaje.contains("exceder") || mensaje.contains("tamaño") || mensaje.contains("5MB")) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .header("X-File-Size-Error", "size-limit-exceeded")
                        .body(MessageResponse.error(mensaje));
            }

            // 🔥 NIVEL 2: 503 SERVICE UNAVAILABLE para errores de Cloudinary
            if (mensaje.contains("Cloudinary") || mensaje.contains("subir la imagen")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .header("X-Service-Error", "cloudinary-unavailable")
                        .header("Retry-After", "60")
                        .body(MessageResponse.error("Servicio de almacenamiento temporalmente no disponible"));
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Upload-Error", "file-processing-failed")
                    .body(MessageResponse.error(mensaje));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al subir la foto de perfil"));
        }
    }

    // 🔥 NIVEL 2: Método auxiliar para calcular completeness del perfil
    private String calcularCompletenessScore(UsuarioResponse usuario) {
        int score = 0;
        int maxScore = 7;

        if (usuario.getNombre() != null && !usuario.getNombre().trim().isEmpty()) score++;
        if (usuario.getEmail() != null && !usuario.getEmail().trim().isEmpty()) score++;
        if (usuario.getFotoPerfil() != null) score++;
        if (usuario.getBio() != null && !usuario.getBio().trim().isEmpty()) score++;
        if (usuario.getUbicacionPreferida() != null) score++;
        if (usuario.getTags() != null && !usuario.getTags().isEmpty()) score++;
        if (usuario.getNivelPescador() != null) score++;

        int percentage = (score * 100) / maxScore;
        return percentage + "%";
    }
    // ========== BÚSQUEDA CON PAGINACIÓN AUTOMATICA - NIVEL 2 ==========

    // LISTAR/BUSCAR USUARIOS (con detección automática de paginación) - NIVEL 2
    @GetMapping
    public ResponseEntity<?> listarUsuarios(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamano,
            HttpServletRequest httpRequest) {

        try {
            // 🔥 NIVEL 2: Validaciones más específicas de paginación
            if (pagina != null && pagina < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-number")
                        .header("X-Min-Page-Number", "0")
                        .body(MessageResponse.error("El número de página no puede ser negativo"));
            }

            if (tamano != null && (tamano <= 0 || tamano > 50)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-size")
                        .header("X-Max-Page-Size", "50")
                        .header("X-Min-Page-Size", "1")
                        .body(MessageResponse.error("El tamaño de página debe estar entre 1 y 50"));
            }

            // 🔥 NIVEL 2: Headers informativos basados en búsqueda
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "public, max-age=120"); // Cache 2 minutos para usuarios

            // DETECCIÓN AUTOMÁTICA: Si vienen parámetros de paginación -> usar versión paginada
            if (pagina != null || tamano != null) {
                // Valores por defecto para paginación
                int paginaFinal = (pagina != null) ? pagina : 0;
                int tamanoFinal = (tamano != null) ? tamano : 10;

                // Usar servicio paginado
                PaginatedResponse<UsuarioResponse> usuarios = usuarioService.buscarUsuariosPaginados(
                        buscar, paginaFinal, tamanoFinal);

                // 🔥 NIVEL 2: Headers específicos para paginación
                headers.add("X-Query-Type", "paginated-users");
                headers.add("X-Page-Number", String.valueOf(paginaFinal));
                headers.add("X-Page-Size", String.valueOf(tamanoFinal));
                headers.add("X-Total-Elements", String.valueOf(usuarios.getTotalElementos()));
                headers.add("X-Total-Pages", String.valueOf(usuarios.getTotalPaginas()));

                if (buscar != null && !buscar.trim().isEmpty()) {
                    headers.add("X-Search-Term", buscar.trim());
                    headers.add("X-Search-Results", String.valueOf(usuarios.getTotalElementos()));
                }

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(usuarios);

            } else {
                //Sin parámetros -> comportamiento original
                List<UsuarioResponse> usuarios = usuarioService.buscarUsuarios(buscar);

                headers.add("X-Query-Type", "full-list-users");
                headers.add("X-Total-Users", String.valueOf(usuarios.size()));

                if (buscar != null && !buscar.trim().isEmpty()) {
                    headers.add("X-Search-Term", buscar.trim());
                }

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(usuarios);
            }

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Search-Error", "users-search-failed")
                    .body(MessageResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al buscar usuarios"));
        }
    }

    // Usuarios por especialidad/tag específico - NIVEL 2
    @GetMapping("/especialidad/{tag}")
    public ResponseEntity<?> obtenerUsuariosPorTag(
            @PathVariable String tag,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            // 🔥 NIVEL 2: Validación más robusta del tag
            if (tag == null || tag.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "empty-tag")
                        .body(MessageResponse.error("Tag no puede estar vacío"));
            }

            if (tag.trim().length() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "tag-too-short")
                        .header("X-Min-Tag-Length", "2")
                        .body(MessageResponse.error("El tag debe tener al menos 2 caracteres"));
            }

            // Validaciones de paginación
            if (pagina < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-number")
                        .body(MessageResponse.error("El número de página no puede ser negativo"));
            }

            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosPorTagPaginados(
                    tag, pagina, tamano);

            // 🔥 NIVEL 2: Headers específicos para especialidades
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Query-Type", "users-by-tag");
            headers.add("X-Search-Tag", tag.trim());
            headers.add("X-Specialists-Found", String.valueOf(usuarios.getTotalElementos()));
            headers.add("X-Page-Number", String.valueOf(pagina));
            headers.add("Cache-Control", "public, max-age=300"); // Cache 5 minutos para especialidades

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuarios);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Tag-Search-Error", "tag-query-failed")
                    .body(MessageResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener usuarios por tag"));
        }
    }

    // Usuarios activos (con actividad reciente) - NIVEL 2
    @GetMapping("/activos")
    public ResponseEntity<?> obtenerUsuariosActivos(
            @RequestParam(defaultValue = "30") int dias,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            // 🔥 NIVEL 2: Validación del rango de días
            if (dias <= 0 || dias > 365) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-days-range")
                        .header("X-Min-Days", "1")
                        .header("X-Max-Days", "365")
                        .body(MessageResponse.error("Los días deben estar entre 1 y 365"));
            }

            if (pagina < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-number")
                        .body(MessageResponse.error("El número de página no puede ser negativo"));
            }

            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosActivosPaginados(
                    dias, pagina, tamano);

            // 🔥 NIVEL 2: Headers para usuarios activos
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Query-Type", "active-users");
            headers.add("X-Activity-Period-Days", String.valueOf(dias));
            headers.add("X-Active-Users-Found", String.valueOf(usuarios.getTotalElementos()));
            headers.add("X-Page-Number", String.valueOf(pagina));
            headers.add("Cache-Control", "public, max-age=180"); // Cache 3 minutos para activos

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuarios);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Activity-Query-Error", "active-users-query-failed")
                    .body(MessageResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener usuarios activos"));
        }
    }

    // Usuarios por nivel de experiencia - NIVEL 2
    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<?> obtenerUsuariosPorNivel(
            @PathVariable String nivel,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            // 🔥 NIVEL 2: Validación más específica del nivel
            if (nivel == null || nivel.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "empty-level")
                        .body(MessageResponse.error("Nivel no puede estar vacío"));
            }

            // 🔥 NIVEL 2: Validar niveles permitidos
            String nivelLimpio = nivel.trim();
            if (!nivelLimpio.equals("Principiante") && !nivelLimpio.equals("Intermedio") && !nivelLimpio.equals("Experto")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-level")
                        .header("X-Valid-Levels", "Principiante,Intermedio,Experto")
                        .body(MessageResponse.error("Nivel debe ser: Principiante, Intermedio o Experto"));
            }

            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosPorNivelPaginados(
                    nivel, pagina, tamano);

            // 🔥 NIVEL 2: Headers específicos por nivel
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Query-Type", "users-by-level");
            headers.add("X-Experience-Level", nivelLimpio);
            headers.add("X-Level-Users-Found", String.valueOf(usuarios.getTotalElementos()));
            headers.add("X-Page-Number", String.valueOf(pagina));
            headers.add("Cache-Control", "public, max-age=600"); // Cache 10 minutos para niveles

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuarios);

        } catch (RuntimeException e) {
            // Tu servicio ya valida niveles válidos
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Level-Query-Error", "level-users-query-failed")
                    .body(MessageResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener usuarios por nivel"));
        }
    }

    // Usuarios por ubicación - NIVEL 2
    @GetMapping("/ubicacion/{ubicacion}")
    public ResponseEntity<?> obtenerUsuariosPorUbicacion(
            @PathVariable String ubicacion,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            // 🔥 NIVEL 2: Validación de ubicación
            if (ubicacion == null || ubicacion.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "empty-location")
                        .body(MessageResponse.error("Ubicación no puede estar vacía"));
            }

            if (ubicacion.trim().length() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "location-too-short")
                        .header("X-Min-Location-Length", "2")
                        .body(MessageResponse.error("La ubicación debe tener al menos 2 caracteres"));
            }

            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosPorUbicacionPaginados(
                    ubicacion, pagina, tamano);

            // 🔥 NIVEL 2: Headers para ubicación
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Query-Type", "users-by-location");
            headers.add("X-Search-Location", ubicacion.trim());
            headers.add("X-Location-Users-Found", String.valueOf(usuarios.getTotalElementos()));
            headers.add("X-Page-Number", String.valueOf(pagina));
            headers.add("Cache-Control", "public, max-age=240"); // Cache 4 minutos para ubicaciones

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuarios);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Location-Query-Error", "location-users-query-failed")
                    .body(MessageResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener usuarios por ubicación"));
        }
    }

    // Usuarios más activos (con más posts) - NIVEL 2
    @GetMapping("/mas-activos")
    public ResponseEntity<?> obtenerUsuariosMasActivos(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            // Validaciones de paginación
            if (pagina < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-number")
                        .body(MessageResponse.error("El número de página no puede ser negativo"));
            }

            if (tamano <= 0 || tamano > 25) { // Límite menor para rankings
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-page-size")
                        .header("X-Max-Page-Size", "25")
                        .body(MessageResponse.error("El tamaño de página debe estar entre 1 y 25 para rankings"));
            }

            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosMasActivosPaginados(
                    pagina, tamano);

            // 🔥 NIVEL 2: Headers específicos para ranking
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Query-Type", "most-active-users-ranking");
            headers.add("X-Sort-Order", "posts-desc");
            headers.add("X-Ranking-Users-Found", String.valueOf(usuarios.getTotalElementos()));
            headers.add("X-Page-Number", String.valueOf(pagina));
            headers.add("Cache-Control", "public, max-age=300"); // Cache 5 minutos para rankings

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuarios);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Ranking-Query-Error", "most-active-query-failed")
                    .body(MessageResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener usuarios más activos"));
        }
    }

    // Usuarios nuevos (registrados recientemente) - NIVEL 2
    @GetMapping("/nuevos")
    public ResponseEntity<?> obtenerUsuariosNuevos(
            @RequestParam(defaultValue = "7") int dias,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            // Validaciones
            if (dias <= 0 || dias > 90) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "invalid-days-range")
                        .header("X-Min-Days", "1")
                        .header("X-Max-Days", "90")
                        .body(MessageResponse.error("Los días deben estar entre 1 y 90"));
            }

            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.obtenerUsuariosNuevosPaginados(
                    dias, pagina, tamano);

            // 🔥 NIVEL 2: Headers para nuevos usuarios
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Query-Type", "new-users");
            headers.add("X-Registration-Period-Days", String.valueOf(dias));
            headers.add("X-New-Users-Found", String.valueOf(usuarios.getTotalElementos()));
            headers.add("X-Page-Number", String.valueOf(pagina));
            headers.add("Cache-Control", "public, max-age=120"); // Cache 2 minutos para nuevos

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuarios);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-New-Users-Query-Error", "new-users-query-failed")
                    .body(MessageResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener usuarios nuevos"));
        }
    }

    // Búsqueda avanzada (busca en múltiples campos) - NIVEL 2
    @GetMapping("/buscar-avanzado")
    public ResponseEntity<?> busquedaAvanzada(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {

        try {
            // ✅ 400 BAD REQUEST - Parámetro de búsqueda requerido
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

            if (q.trim().length() > 50) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .header("X-Validation-Error", "search-query-too-long")
                        .header("X-Max-Query-Length", "50")
                        .body(MessageResponse.error("El término de búsqueda no puede exceder 50 caracteres"));
            }

            PaginatedResponse<UsuarioResponse> usuarios = usuarioService.busquedaAvanzadaPaginada(
                    q, pagina, tamano);

            // 🔥 NIVEL 2: Headers específicos para búsqueda avanzada
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Query-Type", "advanced-user-search");
            headers.add("X-Search-Term", q.trim());
            headers.add("X-Search-Results", String.valueOf(usuarios.getTotalElementos()));
            headers.add("X-Page-Number", String.valueOf(pagina));
            headers.add("X-Search-Fields", "name,email,bio,tags,location");
            headers.add("Cache-Control", "public, max-age=60"); // Cache 1 minuto para búsquedas

            // Si no hay resultados
            if (usuarios.getTotalElementos() == 0) {
                headers.add("X-No-Results", "true");
                headers.add("X-Suggestion", "try-different-terms-or-tags");
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(usuarios);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header("X-Advanced-Search-Error", "advanced-search-failed")
                    .body(MessageResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error en la búsqueda avanzada"));
        }
    }

    // ESTADÍSTICAS PAL' DASHBOARD - NIVEL 2
    @GetMapping("/stats")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsuarios", usuarioService.contarUsuarios());
            stats.put("timestamp", LocalDateTime.now());
            // Futuras estadísticas aquí

            // 🔥 NIVEL 2: Headers para estadísticas
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Stats-Generated", "true");
            headers.add("X-Stats-Timestamp", LocalDateTime.now().toString());
            headers.add("Cache-Control", "public, max-age=300"); // Cache 5 minutos para stats

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stats); // ✅ 200 OK

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.error("Error al obtener estadísticas"));
        }
    }
}