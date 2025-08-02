package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    // Comentarios de un post ordenados por fecha
    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId ORDER BY c.fechaCreacion ASC")
    List<Comentario> findByPostIdOrderByFechaCreacion(@Param("postId") Long postId);

    // Comentarios de un usuario
    List<Comentario> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    // Contar comentarios de un post
    long countByPostId(Long postId);

    // Para respuestas (v2)
    List<Comentario> findByComentarioPadreIdOrderByFechaCreacion(Long comentarioPadreId);

    // Comentarios de un post - PAGINADO
    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId ORDER BY c.fechaCreacion ASC")
    Page<Comentario> findByPostIdOrderByFechaCreacionPaginado(@Param("postId") Long postId, Pageable pageable);

    // Comentarios de un usuario - PAGINADO
    Page<Comentario> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

    // Respuestas de un comentario padre - PAGINADO
    Page<Comentario> findByComentarioPadreIdOrderByFechaCreacion(Long comentarioPadreId, Pageable pageable);

    // Comentarios de un post sin comentarios padre (solo comentarios principales) - PAGINADO
    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId AND c.comentarioPadre IS NULL ORDER BY c.fechaCreacion ASC")
    Page<Comentario> findComentariosPrincipalesByPostId(@Param("postId") Long postId, Pageable pageable);

    // Comentarios de un post incluyendo respuestas - PAGINADO
    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId ORDER BY c.fechaCreacion ASC")
    Page<Comentario> findTodosComentariosByPostId(@Param("postId") Long postId, Pageable pageable);

    // Comentarios recientes de un usuario (para mostrar en el perfil) - PAGINADO
    @Query("SELECT c FROM Comentario c WHERE c.usuario.id = :usuarioId ORDER BY c.fechaCreacion DESC")
    Page<Comentario> findComentariosRecientesByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);
}