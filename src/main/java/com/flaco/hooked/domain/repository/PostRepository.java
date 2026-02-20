package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // ========== SIN PAGINACIÓN (cuidado con memoria) ==========

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    List<Post> findAllByOrderByFechaCreacionDesc();

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    List<Post> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    List<Post> findByCategoriaIdOrderByFechaCreacionDesc(Long categoriaId);

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(p.contenido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "ORDER BY p.fechaCreacion DESC")
    List<Post> buscarPosts(@Param("busqueda") String busqueda);

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    List<Post> findAllByOrderByLikeCountDesc();

    // ========== ESTADÍSTICAS ==========

    Integer countByUsuarioId(Long usuarioId);

    @EntityGraph(attributePaths = {"categoria"})
    List<Post> findByUsuarioId(Long usuarioId);

    // ========== PAGINADOS (recomendados para producción) ==========

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    Page<Post> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    Page<Post> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    Page<Post> findByCategoriaIdOrderByFechaCreacionDesc(Long categoriaId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(p.contenido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "ORDER BY p.fechaCreacion DESC")
    Page<Post> buscarPostsPaginados(@Param("busqueda") String busqueda, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    Page<Post> findAllByOrderByLikeCountDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "categoria"})
    Page<Post> findByUsuarioIdAndCategoriaIdOrderByFechaCreacionDesc(Long usuarioId, Long categoriaId, Pageable pageable);

    // ========== PARA EDICIÓN (carga comentarios) ==========

    @EntityGraph(attributePaths = {"usuario", "categoria", "comentarios"})
    Optional<Post> findWithComentariosById(Long id);
}