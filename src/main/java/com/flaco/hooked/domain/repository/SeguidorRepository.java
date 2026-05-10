package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Seguidor;
import com.flaco.hooked.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeguidorRepository extends JpaRepository<Seguidor, Long> {

    //Follow mutuo ?
    boolean existsBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

    // Encontrar la relación para poder borrarla
    Optional<Seguidor> findBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);

    // Lista de seguidores de un usuario (followers)
    List<Seguidor> findBySeguidoIdOrderByFechaSeguimientoDesc(Long seguidoId);

    // Lista de siguiendo de un usuario (follows - al quien sigo)
    List<Seguidor> findBySeguidorIdOrderByFechaSeguimientoDesc(Long seguidorId);

    long countBySeguidoId(Long seguidoId);

    long countBySeguidorId(Long seguidorId);

    // ¿Son Fishing Buddys? (se siguen mutuamente)
    @Query("SELECT COUNT(s) > 0 FROM Seguidor s WHERE s.seguidor.id = :seguidorId " +
            "AND s.seguido.id = :seguidoId AND EXISTS (" +
            "SELECT s2 FROM Seguidor s2 WHERE s2.seguidor.id = :seguidoId " +
            "AND s2.seguido.id = :seguidorId)")
    boolean esFishingBuddy(@Param("seguidorId") Long seguidorId,
                           @Param("seguidoId") Long seguidoId);

    // Eliminar follow
    @Transactional
    void deleteBySeguidorIdAndSeguidoId(Long seguidorId, Long seguidoId);
}
