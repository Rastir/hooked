package com.flaco.hooked.domain.service;

import com.flaco.hooked.domain.repository.ComentarioRepository;
import com.flaco.hooked.domain.repository.PostRepository;
import com.flaco.hooked.domain.request.ActualizarComentarioRequest;
import com.flaco.hooked.domain.request.CrearComentarioRequest;
import com.flaco.hooked.domain.response.ComentarioResponse;
import com.flaco.hooked.model.Comentario;
import com.flaco.hooked.model.Post;
import com.flaco.hooked.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
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
}