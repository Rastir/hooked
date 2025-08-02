package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    List<Usuario> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(String nombre, String email);

    // Traer todos los usuarios ordenados por fecha de registro
    Page<Usuario> findAllByOrderByFechaRegistroDesc(Pageable pageable);

    // Búsqueda paginada (versión paginada del método de arriba)
    Page<Usuario> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByFechaRegistroDesc(
            String nombre, String email, Pageable pageable);

    // Usuarios por tag específico
    @Query("SELECT u FROM Usuario u WHERE u.tagsString LIKE %:tag% ORDER BY u.fechaRegistro DESC")
    Page<Usuario> findByTagContaining(@Param("tag") String tag, Pageable pageable);

    // Usuarios activos (con actividad en los últimos X días)
    Page<Usuario> findByUltimaActividadAfterOrderByUltimaActividadDesc(
            LocalDateTime fechaLimite, Pageable pageable);

    // Usuarios por nivel de pescador
    Page<Usuario> findByNivelPescadorOrderByFechaRegistroDesc(String nivelPescador, Pageable pageable);

    // Usuarios por ubicación preferida
    Page<Usuario> findByUbicacionPreferidaContainingIgnoreCaseOrderByFechaRegistroDesc(
            String ubicacion, Pageable pageable);

    // Usuarios más activos (con más posts)
    @Query("SELECT u FROM Usuario u LEFT JOIN u.posts p GROUP BY u ORDER BY COUNT(p) DESC, u.fechaRegistro DESC")
    Page<Usuario> findUsuariosMasActivos(Pageable pageable);

    // Usuarios nuevos (registrados recientemente)
    Page<Usuario> findByFechaRegistroAfterOrderByFechaRegistroDesc(
            LocalDateTime fechaLimite, Pageable pageable);

    // Búsqueda avanzada combinada
    @Query("SELECT u FROM Usuario u WHERE " +
            "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(u.ubicacionPreferida) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(u.tagsString) LIKE LOWER(CONCAT('%', :termino, '%'))) " +
            "ORDER BY u.fechaRegistro DESC")
    Page<Usuario> busquedaAvanzada(@Param("termino") String termino, Pageable pageable);
}