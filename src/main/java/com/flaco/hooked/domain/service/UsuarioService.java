package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.request.ActualizarPerfilRequest;
import com.flaco.hooked.domain.request.CrearUsuarioRequest;
import com.flaco.hooked.domain.response.PaginatedResponse;
import com.flaco.hooked.domain.response.UsuarioResponse;
import com.flaco.hooked.model.Usuario;
import com.flaco.hooked.domain.repository.UsuarioRepository;
import com.flaco.hooked.domain.repository.PostRepository;
import com.flaco.hooked.domain.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ImageStorageService imageStorageService;

    // fallback/referencia en caso de fallo del server
    private final String UPLOAD_DIR = "uploads/profiles/";

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario crearUsuario(CrearUsuarioRequest request) {
        // Verificar si el email ya existe (tu lógica original)
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear nuevo usuario (tu lógica original)
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        // Los campos nuevos se inicializan automáticamente en el constructor

        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    // Obtener perfil completo por email (para usuario autenticado)
    public UsuarioResponse obtenerPerfilPorEmail(String email) {
        Usuario usuario = buscarPorEmail(email);
        return convertirAResponse(usuario);
    }

    // Obtener perfil público por ID (para ver otros usuarios)
    public UsuarioResponse obtenerPerfilPublico(Long id) {
        Usuario usuario = buscarPorId(id);
        return convertirAResponse(usuario);
    }

    // Actualizar perfil del usuario autenticado
    public UsuarioResponse actualizarPerfil(String email, ActualizarPerfilRequest request) {
        Usuario usuario = buscarPorEmail(email);

        // Actualizar solo campos no nulos (update parcial inteligente)
        if (request.getNombre() != null && !request.getNombre().trim().isEmpty()) {
            usuario.setNombre(request.getNombre().trim());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Validar email único (excluyendo el usuario actual)
            validarEmailUnico(request.getEmail(), usuario.getId());
            usuario.setEmail(request.getEmail().trim());
        }

        if (request.getBio() != null) {
            usuario.setBio(request.getBio().trim().isEmpty() ? null : request.getBio().trim());
        }

        if (request.getUbicacionPreferida() != null) {
            usuario.setUbicacionPreferida(request.getUbicacionPreferida().trim().isEmpty()
                    ? null : request.getUbicacionPreferida().trim());
        }

        if (request.getTags() != null) {
            usuario.setTags(request.getTags());
        }

        // Cambio de contraseña (si se proporciona)
        if (request.esCambioDeContrasena()) {
            cambiarContrasena(usuario, request.getContrasenaActual(), request.getNuevaContrasena());
        }

        // Actualizar nivel de pescador automáticamente
        actualizarNivelPescador(usuario);

        // Actualizar timestamp de actividad
        usuario.actualizarUltimaActividad();

        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        return convertirAResponse(usuarioActualizado);
    }

    // Buscar usuarios con DTOs seguros
    public List<UsuarioResponse> buscarUsuarios(String termino) {
        List<Usuario> usuarios;

        if (termino == null || termino.trim().isEmpty()) {
            usuarios = usuarioRepository.findAll();
        } else {
            // Buscar por nombre o email (necesitarás agregar este método al repository)
            usuarios = usuarioRepository.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    termino.trim(), termino.trim());
        }

        return usuarios.stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    //Subir foto de perfil (Cloudinary)
    public String subirFotoPerfil(String email, MultipartFile archivo) {
        // validaciones
        if (archivo.isEmpty()) {
            throw new RuntimeException("Archivo de imagen está vacío");
        }

        // Validar tipo de archivo
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen");
        }

        // Validar tamaño (máx 5MB)
        if (archivo.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("La imagen no puede exceder 5MB");
        }

        Usuario usuario = buscarPorEmail(email);

        try {
            // Subir a Cloudinary
            String urlImagenAnterior = usuario.getFotoPerfil();
            String nuevaUrlImagen = imageStorageService.subirImagen(archivo, "profiles");

            // Actualizar usuario con la nueva URL pública
            usuario.setFotoPerfil(nuevaUrlImagen);
            usuario.actualizarUltimaActividad();
            usuarioRepository.save(usuario);

            // Limpiar imagen anterior de Cloudinary
            if (urlImagenAnterior != null && urlImagenAnterior.contains("cloudinary.com")) {
                try {
                    imageStorageService.eliminarImagen(urlImagenAnterior);
                } catch (Exception e) {
                    // Log pero no fallar la operación principal
                    System.err.println("No se pudo eliminar imagen anterior de Cloudinary: " + e.getMessage());
                }
            }

            return nuevaUrlImagen;

        } catch (IOException e) {
            throw new RuntimeException("Error al subir la imagen a Cloudinary: " + e.getMessage());
        }
    }

    // MÉTODOS PRIVADOS DE UTILIDAD

    // Convertir Usuario a UsuarioResponse con estadísticas calculadas
    private UsuarioResponse convertirAResponse(Usuario usuario) {
        UsuarioResponse response = new UsuarioResponse();

        // Campos básicos
        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setEmail(usuario.getEmail());
        response.setFechaRegistro(usuario.getFechaRegistro());

        // Campos de perfil
        response.setFotoPerfil(usuario.getFotoPerfil());
        response.setBio(usuario.getBio());
        response.setTags(usuario.getTags());
        response.setUbicacionPreferida(usuario.getUbicacionPreferida());

        // Estadísticas calculadas en tiempo real
        response.setTotalPosts(postRepository.countByUsuarioId(usuario.getId()));
        response.setTotalLikes(calcularTotalLikes(usuario.getId()));
        response.setTotalComentarios(0); // Preparado para futuro

        // Campos de pescador
        response.setUltimaActividad(usuario.getUltimaActividad());
        response.setNivelPescador(usuario.getNivelPescador());

        return response;
    }

    //Validar que email sea único (excluyendo usuario actual)

    private void validarEmailUnico(String email, Long idExcluir) {
        usuarioRepository.findByEmail(email)
                .filter(usuario -> !usuario.getId().equals(idExcluir))
                .ifPresent(usuario -> {
                    throw new RuntimeException("Email ya está en uso por otro usuario");
                });
    }

    //Cambiar contraseña con validación de seguridad

    private void cambiarContrasena(Usuario usuario, String contrasenaActual, String nuevaContrasena) {
        // Verificar contraseña actual
        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
            throw new RuntimeException("Contraseña actual incorrecta");
        }

        // Encriptar y asignar nueva contraseña
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
    }

    //Calcular total de likes recibidos por el usuario
    private Integer calcularTotalLikes(Long usuarioId) {
        return postRepository.findByUsuarioId(usuarioId)
                .stream()
                .mapToInt(post -> post.getLikeCount() != null ? post.getLikeCount() : 0)
                .sum();
    }


    private void actualizarNivelPescador(Usuario usuario) {
        int totalPosts = postRepository.countByUsuarioId(usuario.getId());
        int totalLikes = calcularTotalLikes(usuario.getId());

        String nuevoNivel;
        if (totalPosts >= 500 || totalLikes >= 1000) {
            nuevoNivel = "Experto";
        } else if (totalPosts >= 250 || totalLikes >= 800) {
            nuevoNivel = "Intermedio";
        } else {
            nuevoNivel = "Principiante";
        }

        usuario.setNivelPescador(nuevoNivel);
    }

    //Obtener extensión del archivo <-- Momentaneo por falta de server
    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return ".jpg"; // Default
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }

    // Eliminar foto anterior del sistema de archivos <-- momentaneo a falta de server
    private void eliminarFotoAnterior(String fotoUrl) {
        try {
            if (fotoUrl.startsWith("/")) {
                fotoUrl = fotoUrl.substring(1); // Quitar "/" inicial
            }
            Path archivoAnterior = Paths.get(fotoUrl);
            if (Files.exists(archivoAnterior)) {
                Files.delete(archivoAnterior);
            }
        } catch (IOException e) {
            // Log error pero no fallar la operación
            System.err.println("Error eliminando foto anterior: " + e.getMessage());
        }
    }

    // Obtener todos los usuarios paginados (ordenados por fecha de registro)
    public PaginatedResponse<UsuarioResponse> obtenerUsuariosPaginados(int pagina, int tamano) {
        // Validaciones inteligentes (límite máximo 50 para usuarios)
        if (pagina < 0) pagina = 0;
        if (tamano <= 0) tamano = 10;
        if (tamano > 50) tamano = 50; // Límite máximo para usuarios

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Usuario> pageUsuarios = usuarioRepository.findAllByOrderByFechaRegistroDesc(pageable);

        // Convertir a UsuarioResponse usando tu método existente
        Page<UsuarioResponse> pageResponse = pageUsuarios.map(this::convertirAResponse);

        return new PaginatedResponse<>(pageResponse);
    }

    // Búsqueda paginada de usuarios
    public PaginatedResponse<UsuarioResponse> buscarUsuariosPaginados(String termino, int pagina, int tamano) {
        // Validaciones inteligentes
        if (pagina < 0) pagina = 0;
        if (tamano <= 0) tamano = 10;
        if (tamano > 50) tamano = 50;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Usuario> pageUsuarios;

        if (termino == null || termino.trim().isEmpty()) {
            // Si no hay término, obtener todos
            pageUsuarios = usuarioRepository.findAllByOrderByFechaRegistroDesc(pageable);
        } else {
            // Buscar por nombre o email (paginado)
            String terminoLimpio = termino.trim();
            pageUsuarios = usuarioRepository.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByFechaRegistroDesc(
                    terminoLimpio, terminoLimpio, pageable);
        }

        Page<UsuarioResponse> pageResponse = pageUsuarios.map(this::convertirAResponse);
        return new PaginatedResponse<>(pageResponse);
    }

    // Usuarios por especialidad/tag específico (paginado)
    public PaginatedResponse<UsuarioResponse> obtenerUsuariosPorTagPaginados(String tag, int pagina, int tamano) {
        if (tag == null || tag.trim().isEmpty()) {
            throw new RuntimeException("Tag no puede estar vacío");
        }

        // Validaciones
        if (pagina < 0) pagina = 0;
        if (tamano <= 0) tamano = 10;
        if (tamano > 50) tamano = 50;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Usuario> pageUsuarios = usuarioRepository.findByTagContaining(tag.trim(), pageable);

        Page<UsuarioResponse> pageResponse = pageUsuarios.map(this::convertirAResponse);
        return new PaginatedResponse<>(pageResponse);
    }

    // Usuarios activos (con actividad reciente) paginados
    public PaginatedResponse<UsuarioResponse> obtenerUsuariosActivosPaginados(int diasActividad, int pagina, int tamano) {
        // Validaciones
        if (pagina < 0) pagina = 0;
        if (tamano <= 0) tamano = 10;
        if (tamano > 50) tamano = 50;
        if (diasActividad <= 0) diasActividad = 30; // Default 30 días

        // Calcular fecha límite
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasActividad);

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Usuario> pageUsuarios = usuarioRepository.findByUltimaActividadAfterOrderByUltimaActividadDesc(
                fechaLimite, pageable);

        Page<UsuarioResponse> pageResponse = pageUsuarios.map(this::convertirAResponse);
        return new PaginatedResponse<>(pageResponse);
    }

    // Usuarios por nivel de pescador (paginado)
    public PaginatedResponse<UsuarioResponse> obtenerUsuariosPorNivelPaginados(String nivel, int pagina, int tamano) {
        if (nivel == null || nivel.trim().isEmpty()) {
            throw new RuntimeException("Nivel de pescador no puede estar vacío");
        }

        // Validar nivel válido
        String nivelLimpio = nivel.trim();
        if (!nivelLimpio.equals("Principiante") && !nivelLimpio.equals("Intermedio") && !nivelLimpio.equals("Experto")) {
            throw new RuntimeException("Nivel debe ser: Principiante, Intermedio o Experto");
        }

        // Validaciones
        if (pagina < 0) pagina = 0;
        if (tamano <= 0) tamano = 10;
        if (tamano > 50) tamano = 50;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Usuario> pageUsuarios = usuarioRepository.findByNivelPescadorOrderByFechaRegistroDesc(nivelLimpio, pageable);

        Page<UsuarioResponse> pageResponse = pageUsuarios.map(this::convertirAResponse);
        return new PaginatedResponse<>(pageResponse);
    }

    // Usuarios por ubicación preferida (paginado)
    public PaginatedResponse<UsuarioResponse> obtenerUsuariosPorUbicacionPaginados(String ubicacion, int pagina, int tamano) {
        if (ubicacion == null || ubicacion.trim().isEmpty()) {
            throw new RuntimeException("Ubicación no puede estar vacía");
        }

        // Validaciones
        if (pagina < 0) pagina = 0;
        if (tamano <= 0) tamano = 10;
        if (tamano > 50) tamano = 50;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Usuario> pageUsuarios = usuarioRepository.findByUbicacionPreferidaContainingIgnoreCaseOrderByFechaRegistroDesc(
                ubicacion.trim(), pageable);

        Page<UsuarioResponse> pageResponse = pageUsuarios.map(this::convertirAResponse);
        return new PaginatedResponse<>(pageResponse);
    }

    // Usuarios más activos (con más posts) paginados
    public PaginatedResponse<UsuarioResponse> obtenerUsuariosMasActivosPaginados(int pagina, int tamano) {
        // Validaciones
        if (pagina < 0) pagina = 0;
        if (tamano <= 0) tamano = 10;
        if (tamano > 50) tamano = 50;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Usuario> pageUsuarios = usuarioRepository.findUsuariosMasActivos(pageable);

        Page<UsuarioResponse> pageResponse = pageUsuarios.map(this::convertirAResponse);
        return new PaginatedResponse<>(pageResponse);
    }

    // Usuarios nuevos (registrados recientemente) paginados
    public PaginatedResponse<UsuarioResponse> obtenerUsuariosNuevosPaginados(int diasRecientes, int pagina, int tamano) {
        // Validaciones
        if (pagina < 0) pagina = 0;
        if (tamano <= 0) tamano = 10;
        if (tamano > 50) tamano = 50;
        if (diasRecientes <= 0) diasRecientes = 7; // Default últimos 7 días

        // Calcular fecha límite
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(diasRecientes);

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Usuario> pageUsuarios = usuarioRepository.findByFechaRegistroAfterOrderByFechaRegistroDesc(
                fechaLimite, pageable);

        Page<UsuarioResponse> pageResponse = pageUsuarios.map(this::convertirAResponse);
        return new PaginatedResponse<>(pageResponse);
    }

    // Búsqueda avanzada combinada (paginada --> Este busca en todas partes)
    public PaginatedResponse<UsuarioResponse> busquedaAvanzadaPaginada(String termino, int pagina, int tamano) {
        if (termino == null || termino.trim().isEmpty()) {
            throw new RuntimeException("Término de búsqueda no puede estar vacío");
        }

        // Validaciones
        if (pagina < 0) pagina = 0;
        if (tamano <= 0) tamano = 10;
        if (tamano > 50) tamano = 50;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Usuario> pageUsuarios = usuarioRepository.busquedaAvanzada(termino.trim(), pageable);

        Page<UsuarioResponse> pageResponse = pageUsuarios.map(this::convertirAResponse);
        return new PaginatedResponse<>(pageResponse);
    }
}