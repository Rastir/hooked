package com.flaco.hooked.domain.service;

import com.flaco.hooked.model.Categoria;
import com.flaco.hooked.domain.repository.CategoriaRepository;
import com.flaco.hooked.model.Like;
import com.flaco.hooked.domain.repository.LikeRepository;
import com.flaco.hooked.model.Post;
import com.flaco.hooked.domain.repository.PostRepository;
import com.flaco.hooked.domain.request.ActualizarPostRequest;
import com.flaco.hooked.domain.request.CrearPostRequest;
import com.flaco.hooked.domain.response.PostResponse;
import com.flaco.hooked.domain.response.PaginatedResponse;
import com.flaco.hooked.model.Usuario;
import com.flaco.hooked.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private LikeRepository likeRepository;

    // ========== CRUD BÁSICO ==========

    // Crear post
    public PostResponse crearPost(CrearPostRequest request, String emailUsuario) {
        Usuario autor = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Post post = new Post();
        post.setTitulo(request.getTitulo());
        post.setContenido(request.getContenido());
        post.setFotoLink(request.getFotoLink());
        post.setUsuario(autor);
        post.setCategoria(categoria);
        post.setLikeCount(0);

        Post postGuardado = postRepository.save(post);
        return convertirAResponse(postGuardado, autor.getId());
    }

    // Obtener todos los posts
    public List<PostResponse> obtenerTodosPosts() {
        return postRepository.findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(post -> convertirAResponse(post, null))
                .collect(Collectors.toList());
    }

    // Obtener post por ID
    public PostResponse obtenerPostPorId(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));
        return convertirAResponse(post, null);
    }

    // Obtener posts por usuario ID
    public List<PostResponse> obtenerPostsPorUsuario(Long usuarioId) {
        return postRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId)
                .stream()
                .map(post -> convertirAResponse(post, null))
                .collect(Collectors.toList());
    }

    // Obtener posts por categoría
    public List<PostResponse> obtenerPostsPorCategoria(Long categoriaId) {
        return postRepository.findByCategoriaIdOrderByFechaCreacionDesc(categoriaId)
                .stream()
                .map(post -> convertirAResponse(post, null))
                .collect(Collectors.toList());
    }

    // Obtener posts por email del usuario
    public List<PostResponse> obtenerPostsPorUsuario(String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return postRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId())
                .stream()
                .map(post -> convertirAResponse(post, usuario.getId()))
                .collect(Collectors.toList());
    }

    // Actualizar post
    public PostResponse actualizarPost(Long id, ActualizarPostRequest request, String emailUsuario) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        if (!post.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permisos para editar este post");
        }

        if (request.getTitulo() != null) {
            post.setTitulo(request.getTitulo());
        }
        if (request.getContenido() != null) {
            post.setContenido(request.getContenido());
        }
        if (request.getFotoLink() != null) {
            post.setFotoLink(request.getFotoLink());
        }
        if (request.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            post.setCategoria(categoria);
        }

        Post postActualizado = postRepository.save(post);
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario).orElse(null);
        return convertirAResponse(postActualizado, usuario != null ? usuario.getId() : null);
    }

    // Eliminar post
    public void eliminarPost(Long id, String emailUsuario) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        if (!post.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permisos para eliminar este post");
        }

        // Eliminar likes asociados primero
        likeRepository.deleteByPostId(id);

        postRepository.delete(post);
    }

    // ========== LIKES CON TOGGLE CORREGIDO ==========

    /**
     * CORREGIDO: Toggle like con sincronización forzada a BD
     */
    public PostResponse toggleLike(Long postId, Long usuarioId, String emailUsuario) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        // Si no tenemos el ID, buscar por email (fallback)
        Long userIdToUse = usuarioId;
        if (userIdToUse == null && emailUsuario != null) {
            Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            userIdToUse = usuario.getId();
        }

        if (userIdToUse == null) {
            throw new RuntimeException("No se pudo identificar al usuario");
        }

        // ✅ CORREGIDO: Forzar sincronización antes de verificar
        likeRepository.flush();

        boolean yaDioLike = likeRepository.existsByUsuarioIdAndPostId(userIdToUse, postId);
        System.out.println("DEBUG - toggleLike inicial: usuario=" + userIdToUse + " post=" + postId + " yaDioLike=" + yaDioLike);

        if (yaDioLike) {
            // Quitar like
            likeRepository.deleteByUsuarioIdAndPostId(userIdToUse, postId);
            System.out.println("DEBUG - Like eliminado para usuario: " + userIdToUse + " post: " + postId);
        } else {
            // Dar like
            Like like = new Like();
            Usuario usuarioRef = usuarioRepository.findById(userIdToUse)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            like.setUsuario(usuarioRef);
            like.setPost(post);
            likeRepository.save(like);
            System.out.println("DEBUG - Like creado para usuario: " + userIdToUse + " post: " + postId);
        }

        // ✅ CORREGIDO: Forzar flush y limpiar caché de Hibernate
        likeRepository.flush();

        // Recalcular contador desde la base de datos
        Long totalLikes = likeRepository.countByPostId(postId);
        post.setLikeCount(totalLikes.intValue());
        postRepository.save(post);
        postRepository.flush();

        System.out.println("DEBUG - Total likes después de toggle: " + totalLikes);

        // ✅ CORREGIDO: Recargar post completamente desde BD (no usar caché)
        Post postActualizado = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        // Verificar estado final
        boolean likeActual = likeRepository.existsByUsuarioIdAndPostId(userIdToUse, postId);
        System.out.println("DEBUG - Estado final del like: " + likeActual);

        return convertirAResponse(postActualizado, userIdToUse);
    }

    /**
     * LEGACY: Mantener compatibilidad con código antiguo que solo pasa email
     */
    public PostResponse toggleLike(Long postId, String emailUsuario) {
        return toggleLike(postId, null, emailUsuario);
    }

    // Mantener por compatibilidad
    public PostResponse darLike(Long postId, String emailUsuario) {
        return toggleLike(postId, emailUsuario);
    }

    /**
     * CORREGIDO: Quitar like con sincronización forzada
     */
    public PostResponse quitarLike(Long postId, Long usuarioId, String emailUsuario) {
        // Si no tenemos el ID, buscar por email (fallback)
        Long userIdToUse = usuarioId;
        if (userIdToUse == null && emailUsuario != null) {
            Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            userIdToUse = usuario.getId();
        }

        if (userIdToUse == null) {
            throw new RuntimeException("No se pudo identificar al usuario");
        }

        // ✅ CORREGIDO: Usar delete directo con flush
        likeRepository.deleteByUsuarioIdAndPostId(userIdToUse, postId);
        likeRepository.flush();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        Long totalLikes = likeRepository.countByPostId(postId);
        post.setLikeCount(totalLikes.intValue());
        postRepository.save(post);
        postRepository.flush();

        // Recargar post desde BD
        Post postActualizado = postRepository.findById(postId).orElse(post);

        return convertirAResponse(postActualizado, userIdToUse);
    }

    /**
     * LEGACY: Mantener compatibilidad
     */
    public PostResponse quitarLike(Long postId, String emailUsuario) {
        return quitarLike(postId, null, emailUsuario);
    }

    // ========== MÉTODOS PAGINADOS CORREGIDOS ==========

    /**
     * CORREGIDO: Ahora recibe Long usuarioId en lugar de String emailUsuario
     */
    public PaginatedResponse<PostResponse> obtenerTodosPostsPaginados(int pagina, int tamano, Long usuarioId) {
        if (tamano > 50) tamano = 50;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.findAllByOrderByFechaCreacionDesc(pageable);

        // CORREGIDO: Usar usuarioId directamente, sin buscar en BD
        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioId));

        return new PaginatedResponse<>(postResponsePage);
    }

    /**
     * CORREGIDO: Ahora recibe Long usuarioId en lugar de String emailUsuario
     */
    public PaginatedResponse<PostResponse> obtenerPostsPorCategoriaPaginados(Long categoriaId, int pagina, int tamano, Long usuarioId) {
        if (tamano > 50) tamano = 50;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.findByCategoriaIdOrderByFechaCreacionDesc(categoriaId, pageable);

        // CORREGIDO: Usar usuarioId directamente
        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioId));

        return new PaginatedResponse<>(postResponsePage);
    }

    /**
     * CORREGIDO: Ahora recibe Long usuarioActualId en lugar de String emailUsuarioActual
     */
    public PaginatedResponse<PostResponse> obtenerPostsPorUsuarioPaginados(Long usuarioId, int pagina, int tamano, Long usuarioActualId) {
        if (tamano > 50) tamano = 50;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId, pageable);

        // CORREGIDO: Usar usuarioActualId directamente, sin buscar en BD
        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioActualId));

        return new PaginatedResponse<>(postResponsePage);
    }

    /**
     * CORREGIDO: Ahora recibe Long usuarioId en lugar de String emailUsuario
     */
    public PaginatedResponse<PostResponse> buscarPostsPaginados(String busqueda, int pagina, int tamano, Long usuarioId) {
        if (tamano > 50) tamano = 50;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.buscarPostsPaginados(busqueda, pageable);

        // CORREGIDO: Usar usuarioId directamente
        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioId));

        return new PaginatedResponse<>(postResponsePage);
    }

    /**
     * CORREGIDO: Ahora recibe Long usuarioId en lugar de String emailUsuario
     */
    public PaginatedResponse<PostResponse> obtenerPostsPopularesPaginados(int pagina, int tamano, Long usuarioId) {
        if (tamano > 50) tamano = 50;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.findAllByOrderByLikeCountDesc(pageable);

        // CORREGIDO: Usar usuarioId directamente
        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioId));

        return new PaginatedResponse<>(postResponsePage);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private PostResponse convertirAResponse(Post post) {
        return convertirAResponse(post, null);
    }

    /**
     * ✅ CORREGIDO: Verificación de like con sincronización forzada
     */
    private PostResponse convertirAResponse(Post post, Long usuarioActualId) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitulo(post.getTitulo());
        response.setContenido(post.getContenido());
        response.setFotoLink(post.getFotoLink());
        response.setFechaCreacion(post.getFechaCreacion());
        response.setLikeCount(post.getLikeCount());

        // Convertir Usuario
        PostResponse.UsuarioResponse usuarioResponse = new PostResponse.UsuarioResponse();
        usuarioResponse.setId(post.getUsuario().getId());
        usuarioResponse.setNombre(post.getUsuario().getNombre());
        usuarioResponse.setEmail(post.getUsuario().getEmail());
        response.setAutor(usuarioResponse);

        // Convertir Categoria
        PostResponse.CategoriaResponse categoriaResponse = new PostResponse.CategoriaResponse();
        categoriaResponse.setId(post.getCategoria().getId());
        categoriaResponse.setNombre(post.getCategoria().getNombre());
        response.setCategoria(categoriaResponse);

        // Contar comentarios
        response.setComentariosCount(post.getComentarios() != null
                ? (long) post.getComentarios().size()
                : 0L);

        // ✅ CORREGIDO: Verificar like con flush forzado y recarga desde BD
        boolean dioLike = false;
        if (usuarioActualId != null) {
            // Forzar sincronización de cualquier operación pendiente
            likeRepository.flush();

            // Usar count en lugar de exists (más confiable con Hibernate)
            long likeCount = likeRepository.countByUsuarioIdAndPostId(usuarioActualId, post.getId());
            dioLike = likeCount > 0;

            System.out.println("DEBUG - convertirAResponse: usuario=" + usuarioActualId +
                    " post=" + post.getId() + " likeCount=" + likeCount + " dioLike=" + dioLike);
        }
        response.setLikedByCurrentUser(dioLike);

        return response;
    }
}