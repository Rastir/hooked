package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Comentario;
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
}