package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    // ========== BÚSQUEDAS BÁSICAS ==========

    @EntityGraph(attributePaths = {"posts"})
    List<Usuario> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombre, String email);

    @EntityGraph(attributePaths = {"posts"})
    Page<Usuario> findAllByOrderByFechaRegistroDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"posts"})
    Page<Usuario> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByFechaRegistroDesc(
            String nombre, String email, Pageable pageable);

    // ========== FILTROS ESPECIALIZADOS ==========

    @Query("SELECT u FROM Usuario u WHERE u.tagsString LIKE %:tag% ORDER BY u.fechaRegistro DESC")
    Page<Usuario> findByTagContaining(@Param("tag") String tag, Pageable pageable);

    Page<Usuario> findByUltimaActividadAfterOrderByUltimaActividadDesc(
            LocalDateTime fechaLimite, Pageable pageable);

    Page<Usuario> findByNivelPescadorOrderByFechaRegistroDesc(String nivelPescador, Pageable pageable);

    Page<Usuario> findByUbicacionPreferidaContainingIgnoreCaseOrderByFechaRegistroDesc(
            String ubicacion, Pageable pageable);

    // ========== RANKING (OPTIMIZADO) ==========

    // Versión optimizada: usa subquery en lugar de GROUP BY en memoria
    @Query(value = """
        SELECT u.* FROM usuarios u 
        LEFT JOIN (
            SELECT usuario_id, COUNT(*) as post_count 
            FROM posts 
            GROUP BY usuario_id
        ) p ON u.id = p.usuario_id 
        ORDER BY COALESCE(p.post_count, 0) DESC, u.fecha_registro DESC
        """, nativeQuery = true)
    Page<Usuario> findUsuariosMasActivos(Pageable pageable);

    // ========== RECIENTES Y BÚSQUEDA ==========

    Page<Usuario> findByFechaRegistroAfterOrderByFechaRegistroDesc(
            LocalDateTime fechaLimite, Pageable pageable);

    @Query("SELECT u FROM Usuario u WHERE " +
            "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(u.ubicacionPreferida) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(u.tagsString) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
            "ORDER BY u.fechaRegistro DESC")
    Page<Usuario> busquedaAvanzada(@Param("termino") String termino, Pageable pageable);
}