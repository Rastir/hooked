package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUsuarioIdAndPostId(Long usuarioId, Long postId);

    Optional<Like> findByUsuarioIdAndPostId(Long usuarioId, Long postId);

    Long countByPostId(Long postId);
    // NUEVO: Método para eliminar directo sin buscar primero
    @Transactional
    void deleteByUsuarioIdAndPostId(Long usuarioId, Long postId);

    @Transactional
    void deleteByPostId(Long postId);
}
