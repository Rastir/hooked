package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    // ========== CON USUARIO (evita N+1) ==========

    @EntityGraph(attributePaths = {"usuario"})
    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId ORDER BY c.fechaCreacion ASC")
    List<Comentario> findByPostIdOrderByFechaCreacion(@Param("postId") Long postId);

    @EntityGraph(attributePaths = {"usuario", "post"})
    List<Comentario> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "comentarioPadre"})
    List<Comentario> findByComentarioPadreIdOrderByFechaCreacion(Long comentarioPadreId);

    // ========== PAGINADOS (producción) ==========

    @EntityGraph(attributePaths = {"usuario"})
    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId ORDER BY c.fechaCreacion ASC")
    Page<Comentario> findByPostIdOrderByFechaCreacionPaginado(@Param("postId") Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "post"})
    Page<Comentario> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "comentarioPadre"})
    Page<Comentario> findByComentarioPadreIdOrderByFechaCreacion(Long comentarioPadreId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario"})
    @Query("SELECT c FROM Comentario c WHERE c.post.id = :postId AND c.comentarioPadre IS NULL ORDER BY c.fechaCreacion ASC")
    Page<Comentario> findComentariosPrincipalesByPostId(@Param("postId") Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario"})
    @Query("SELECT c FROM Comentario c WHERE c.usuario.id = :usuarioId ORDER BY c.fechaCreacion DESC")
    Page<Comentario> findComentariosRecientesByUsuarioId(@Param("usuarioId") Long usuarioId, Pageable pageable);

    // ========== CONTADORES (optimizados) ==========

    long countByPostId(Long postId);

    @Query("SELECT COUNT(c) FROM Comentario c WHERE c.comentarioPadre.id = :comentarioId")
    long countRespuestasByComentarioId(@Param("comentarioId") Long comentarioId);
}