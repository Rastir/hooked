package com.flaco.hooked.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    List<Post> findAllByOrderByFechaCreacionDesc();
    List<Post> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Post> findByCategoriaIdOrderByFechaCreacionDesc(Long categoriaId);
}
