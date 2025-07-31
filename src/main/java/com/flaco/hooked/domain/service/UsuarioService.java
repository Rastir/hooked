package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.request.ActualizarPerfilRequest;
import com.flaco.hooked.domain.request.CrearUsuarioRequest;
import com.flaco.hooked.domain.response.UsuarioResponse;
import com.flaco.hooked.model.Usuario;
import com.flaco.hooked.domain.repository.UsuarioRepository;
import com.flaco.hooked.domain.repository.PostRepository;
import com.flaco.hooked.domain.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    // 📁 Directorio para fotos de perfil (100% casero)
    private final String UPLOAD_DIR = "uploads/profiles/";

    // ✅ MÉTODOS ORIGINALES (mantenidos exactamente igual)

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

    // 🆕 NUEVOS MÉTODOS DE PERFIL (Enterprise Level)

    /**
     * Obtener perfil completo por email (para usuario autenticado)
     */
    public UsuarioResponse obtenerPerfilPorEmail(String email) {
        Usuario usuario = buscarPorEmail(email);
        return convertirAResponse(usuario);
    }

    /**
     * Obtener perfil público por ID (para ver otros usuarios)
     */
    public UsuarioResponse obtenerPerfilPublico(Long id) {
        Usuario usuario = buscarPorId(id);
        return convertirAResponse(usuario);
    }

    /**
     * Actualizar perfil del usuario autenticado
     */
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

    /**
     * Buscar usuarios con DTOs seguros
     */
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

    /**
     * Subir foto de perfil (100% casero - sin AWS)
     */
    public String subirFotoPerfil(String email, MultipartFile archivo) {
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
            // Crear directorio si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generar nombre único para la imagen
            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreArchivo = "usuario_" + usuario.getId() + "_" + UUID.randomUUID() + extension;
            Path archivoPath = uploadPath.resolve(nombreArchivo);

            // Eliminar foto anterior si existe
            if (usuario.tieneFotoPerfil()) {
                eliminarFotoAnterior(usuario.getFotoPerfil());
            }

            // Guardar nueva foto
            Files.copy(archivo.getInputStream(), archivoPath);

            // Actualizar usuario
            String fotoUrl = "/" + UPLOAD_DIR + nombreArchivo;
            usuario.setFotoPerfil(fotoUrl);
            usuario.actualizarUltimaActividad();
            usuarioRepository.save(usuario);

            return fotoUrl;

        } catch (IOException e) {
            throw new RuntimeException("Error al subir la imagen: " + e.getMessage());
        }
    }

    // 🔧 MÉTODOS PRIVADOS DE UTILIDAD (la magia interna)

    /**
     * Convertir Usuario a UsuarioResponse con estadísticas calculadas
     */
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
}