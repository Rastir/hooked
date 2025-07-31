package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    // TUS LOS MÉTODOS
    List<Post> findAllByOrderByFechaCreacionDesc();
    List<Post> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Post> findByCategoriaIdOrderByFechaCreacionDesc(Long categoriaId);
    Page<Post> findAllByOrderByFechaCreacionDesc(Pageable pageable);
    Page<Post> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId, Pageable pageable);
    Page<Post> findByCategoriaIdOrderByFechaCreacionDesc(Long categoriaId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
            "LOWER(p.contenido) LIKE LOWER(CONCAT('%', :busqueda, '%')) " +
            "ORDER BY p.fechaCreacion DESC")
    List<Post> buscarPosts(@Param("busqueda") String busqueda);

    List<Post> findAllByOrderByLikeCountDesc();
    List<Post> findByUsuarioIdAndCategoriaIdOrderByFechaCreacionDesc(Long usuarioId, Long categoriaId);

    Integer countByUsuarioId(Long usuarioId);
    List<Post> findByUsuarioId(Long usuarioId);
}