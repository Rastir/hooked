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

    // Obtener posts por usuario
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

        // Debug
        System.out.println("Email del token: " + emailUsuario);
        System.out.println("Email del autor del post: " + post.getUsuario().getEmail());
        System.out.println("¿Son iguales? " + post.getUsuario().getEmail().equals(emailUsuario));

        // Verificar que el usuario sea el dueño del post
        if (!post.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permisos para editar este post");
        }

        // Actualizar solo los campos que vienen en el request
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

        // Verificar que el usuario sea el dueño del post
        if (!post.getUsuario().getEmail().equals(emailUsuario)) {
            throw new RuntimeException("No tienes permisos para eliminar este post");
        }

        postRepository.delete(post);
    }

    // ========== LIKES CON TOGGLE ==========

    // NUEVO: Toggle like (crea si no existe, elimina si existe)
    public PostResponse toggleLike(Long postId, String emailUsuario) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        boolean yaDioLike = likeRepository.existsByUsuarioIdAndPostId(usuario.getId(), postId);

        if (yaDioLike) {
            // Quitar like
            Like like = likeRepository.findByUsuarioIdAndPostId(usuario.getId(), postId)
                    .orElseThrow(() -> new RuntimeException("Like no encontrado"));
            likeRepository.delete(like);
        } else {
            // Dar like
            Like like = new Like();
            like.setUsuario(usuario);
            like.setPost(post);
            likeRepository.save(like);
        }

        // Actualizar contador
        Long totalLikes = likeRepository.countByPostId(postId);
        post.setLikeCount(totalLikes.intValue());
        Post postActualizado = postRepository.save(post);

        // Convertir a response indicando el estado actual del like
        return convertirAResponse(postActualizado, usuario.getId());
    }

    // Mantener por compatibilidad (opcional)
    public PostResponse darLike(Long postId, String emailUsuario) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si ya dio like
        if (likeRepository.existsByUsuarioIdAndPostId(usuario.getId(), postId)) {
            throw new RuntimeException("Ya has dado like a este post");
        }

        // Metodo pa' poner un like
        Like like = new Like();
        like.setUsuario(usuario);
        like.setPost(post);
        likeRepository.save(like);

        // Actualizar contador
        Long totalLikes = likeRepository.countByPostId(postId);
        post.setLikeCount(totalLikes.intValue());
        Post postActualizado = postRepository.save(post);

        return convertirAResponse(postActualizado, usuario.getId());
    }

    // Método pa' quitar el like
    public PostResponse quitarLike(Long postId, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Like like = likeRepository.findByUsuarioIdAndPostId(usuario.getId(), postId)
                .orElseThrow(() -> new RuntimeException("No has dado like a este post"));

        likeRepository.delete(like);

        // Actualizar contador
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        Long totalLikes = likeRepository.countByPostId(postId);
        post.setLikeCount(totalLikes.intValue());
        Post postActualizado = postRepository.save(post);

        return convertirAResponse(postActualizado, usuario.getId());
    }

    // MÉTODOS PAGINADOS

    // Obtener todos los posts - PAGINADO
    public PaginatedResponse<PostResponse> obtenerTodosPostsPaginados(int pagina, int tamano, String emailUsuario) {
        // Validar parámetros
        if (tamano > 50) tamano = 50; // Límite máximo
        if (pagina < 0) pagina = 0;   // No páginas negativas

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.findAllByOrderByFechaCreacionDesc(pageable);

        Usuario usuario = emailUsuario != null ?
                usuarioRepository.findByEmail(emailUsuario).orElse(null) : null;
        Long usuarioId = usuario != null ? usuario.getId() : null;

        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioId));

        return new PaginatedResponse<>(postResponsePage);
    }

    // Obtener posts por categoría - PAGINADO
    public PaginatedResponse<PostResponse> obtenerPostsPorCategoriaPaginados(Long categoriaId, int pagina, int tamano, String emailUsuario) {
        if (tamano > 50) tamano = 50;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.findByCategoriaIdOrderByFechaCreacionDesc(categoriaId, pageable);

        Usuario usuario = emailUsuario != null ?
                usuarioRepository.findByEmail(emailUsuario).orElse(null) : null;
        Long usuarioId = usuario != null ? usuario.getId() : null;

        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioId));

        return new PaginatedResponse<>(postResponsePage);
    }

    // Obtener posts por usuario - PAGINADO
    public PaginatedResponse<PostResponse> obtenerPostsPorUsuarioPaginados(Long usuarioId, int pagina, int tamano, String emailUsuarioActual) {
        if (tamano > 50) tamano = 50;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId, pageable);

        Usuario usuarioActual = emailUsuarioActual != null ?
                usuarioRepository.findByEmail(emailUsuarioActual).orElse(null) : null;
        Long usuarioActualId = usuarioActual != null ? usuarioActual.getId() : null;

        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioActualId));

        return new PaginatedResponse<>(postResponsePage);
    }

    // Buscar posts - PAGINADO
    public PaginatedResponse<PostResponse> buscarPostsPaginados(String busqueda, int pagina, int tamano, String emailUsuario) {
        if (tamano > 50) tamano = 50;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.buscarPostsPaginados(busqueda, pageable);

        Usuario usuario = emailUsuario != null ?
                usuarioRepository.findByEmail(emailUsuario).orElse(null) : null;
        Long usuarioId = usuario != null ? usuario.getId() : null;

        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioId));

        return new PaginatedResponse<>(postResponsePage);
    }

    // Posts más populares - PAGINADO
    public PaginatedResponse<PostResponse> obtenerPostsPopularesPaginados(int pagina, int tamano, String emailUsuario) {
        if (tamano > 50) tamano = 50;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Post> postPage = postRepository.findAllByOrderByLikeCountDesc(pageable);

        Usuario usuario = emailUsuario != null ?
                usuarioRepository.findByEmail(emailUsuario).orElse(null) : null;
        Long usuarioId = usuario != null ? usuario.getId() : null;

        Page<PostResponse> postResponsePage = postPage.map(post -> convertirAResponse(post, usuarioId));

        return new PaginatedResponse<>(postResponsePage);
    }

    // ========== MÉTODOS AUXILIARES ==========

    // Sobrecarga para mantener compatibilidad
    private PostResponse convertirAResponse(Post post) {
        return convertirAResponse(post, null);
    }

    // Método principal que recibe el ID del usuario actual
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

        // Verificar si el usuario actual dio like
        if (usuarioActualId != null) {
            boolean dioLike = likeRepository.existsByUsuarioIdAndPostId(usuarioActualId, post.getId());
            response.setLikedByCurrentUser(dioLike);
        } else {
            response.setLikedByCurrentUser(false);
        }

        return response;
    }
}