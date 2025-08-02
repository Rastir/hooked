package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.repository.ComentarioRepository;
import com.flaco.hooked.domain.repository.PostRepository;
import com.flaco.hooked.domain.request.ActualizarComentarioRequest;
import com.flaco.hooked.domain.request.CrearComentarioRequest;
import com.flaco.hooked.domain.response.ComentarioResponse;
import com.flaco.hooked.domain.response.PaginatedResponse;
import com.flaco.hooked.model.Comentario;
import com.flaco.hooked.model.Post;
import com.flaco.hooked.model.Usuario;
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
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private PostRepository postRepository;

    // Crear comentario
    public ComentarioResponse crearComentario(CrearComentarioRequest request, Usuario usuario) {
        // Verificar que el post existe
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post no encontrado"));

        // Crear comentario
        Comentario comentario = new Comentario(request.getContenido(), usuario, post);

        // Si es respuesta a otro comentario
        if (request.getComentarioPadreId() != null) {
            Comentario comentarioPadre = comentarioRepository.findById(request.getComentarioPadreId())
                    .orElseThrow(() -> new RuntimeException("Comentario padre no encontrado"));
            comentario.setComentarioPadre(comentarioPadre);
        }

        comentario = comentarioRepository.save(comentario);
        return new ComentarioResponse(comentario);
    }

    // Obtener comentarios de un post
    @Transactional(readOnly = true)
    public List<ComentarioResponse> obtenerComentariosPorPost(Long postId) {
        List<Comentario> comentarios = comentarioRepository.findByPostIdOrderByFechaCreacion(postId);
        return comentarios.stream()
                .map(ComentarioResponse::new)
                .collect(Collectors.toList());
    }

    // Obtener comentario por ID
    @Transactional(readOnly = true)
    public ComentarioResponse obtenerComentario(Long id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        return new ComentarioResponse(comentario);
    }

    // Actualizar comentario (solo el autor)
    public ComentarioResponse actualizarComentario(Long id, ActualizarComentarioRequest request, Usuario usuario) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        // Verificar que el usuario es el autor
        if (!comentario.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para editar este comentario");
        }

        comentario.setContenido(request.getContenido());
        comentario = comentarioRepository.save(comentario);
        return new ComentarioResponse(comentario);
    }

    // Eliminar comentario (solo el autor)
    public void eliminarComentario(Long id, Usuario usuario) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        // Verificar que el usuario es el autor
        if (!comentario.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para eliminar este comentario");
        }

        comentarioRepository.delete(comentario);
    }

    // Obtener comentarios de un usuario
    @Transactional(readOnly = true)
    public List<ComentarioResponse> obtenerComentariosPorUsuario(Long usuarioId) {
        List<Comentario> comentarios = comentarioRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
        return comentarios.stream()
                .map(ComentarioResponse::new)
                .collect(Collectors.toList());
    }

    // Contar comentarios de un post
    @Transactional(readOnly = true)
    public long contarComentariosPorPost(Long postId) {
        return comentarioRepository.countByPostId(postId);
    }

    // Obtener comentarios de un post - PAGINADO
    @Transactional(readOnly = true)
    public PaginatedResponse<ComentarioResponse> obtenerComentariosPorPostPaginados(Long postId, int pagina, int tamano) {
        // Validar parámetros
        if (tamano > 100) tamano = 100; // Límite mayor para comentarios
        if (pagina < 0) pagina = 0;

        // Verificar que el post existe
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post no encontrado");
        }

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Comentario> comentarioPage = comentarioRepository.findByPostIdOrderByFechaCreacionPaginado(postId, pageable);
        Page<ComentarioResponse> comentarioResponsePage = comentarioPage.map(ComentarioResponse::new);

        return new PaginatedResponse<>(comentarioResponsePage);
    }

    // Obtener solo comentarios principales (sin respuestas) - PAGINADO
    @Transactional(readOnly = true)
    public PaginatedResponse<ComentarioResponse> obtenerComentariosPrincipalesPorPostPaginados(Long postId, int pagina, int tamano) {
        if (tamano > 100) tamano = 100;
        if (pagina < 0) pagina = 0;

        // Verificar que el post existe
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post no encontrado");
        }

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Comentario> comentarioPage = comentarioRepository.findComentariosPrincipalesByPostId(postId, pageable);
        Page<ComentarioResponse> comentarioResponsePage = comentarioPage.map(ComentarioResponse::new);

        return new PaginatedResponse<>(comentarioResponsePage);
    }

    // Obtener comentarios de un usuario - PAGINADO
    @Transactional(readOnly = true)
    public PaginatedResponse<ComentarioResponse> obtenerComentariosPorUsuarioPaginados(Long usuarioId, int pagina, int tamano) {
        if (tamano > 100) tamano = 100;
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Comentario> comentarioPage = comentarioRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId, pageable);
        Page<ComentarioResponse> comentarioResponsePage = comentarioPage.map(ComentarioResponse::new);

        return new PaginatedResponse<>(comentarioResponsePage);
    }

    // Obtener respuestas de un comentario - PAGINADO
    @Transactional(readOnly = true)
    public PaginatedResponse<ComentarioResponse> obtenerRespuestasPaginadas(Long comentarioPadreId, int pagina, int tamano) {
        if (tamano > 50) tamano = 50; // Límite menor para respuestas
        if (pagina < 0) pagina = 0;

        // Verificar que el comentario padre existe
        if (!comentarioRepository.existsById(comentarioPadreId)) {
            throw new RuntimeException("Comentario padre no encontrado");
        }

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Comentario> respuestaPage = comentarioRepository.findByComentarioPadreIdOrderByFechaCreacion(comentarioPadreId, pageable);
        Page<ComentarioResponse> respuestaResponsePage = respuestaPage.map(ComentarioResponse::new);

        return new PaginatedResponse<>(respuestaResponsePage);
    }

    // Obtener comentarios recientes de un usuario (para perfil) - PAGINADO
    @Transactional(readOnly = true)
    public PaginatedResponse<ComentarioResponse> obtenerComentariosRecientesPorUsuarioPaginados(Long usuarioId, int pagina, int tamano) {
        if (tamano > 50) tamano = 50; // Límite para vistas de perfil
        if (pagina < 0) pagina = 0;

        Pageable pageable = PageRequest.of(pagina, tamano);
        Page<Comentario> comentarioPage = comentarioRepository.findComentariosRecientesByUsuarioId(usuarioId, pageable);
        Page<ComentarioResponse> comentarioResponsePage = comentarioPage.map(ComentarioResponse::new);

        return new PaginatedResponse<>(comentarioResponsePage);
    }
}