package com.flaco.hooked.domain.repository;

import com.flaco.hooked.domain.refreshtoken.RefreshToken;
import com.flaco.hooked.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndActivoTrue(String token);

    List<RefreshToken> findByUsuarioAndActivoTrueOrderByFechaCreacionDesc(Usuario usuario);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.activo = false WHERE rt.usuario = :usuario")
    void desactivarTodosTokensDelUsuario(@Param("usuario") Usuario usuario);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.fechaExpiracion < :fechaLimite OR rt.activo = false")
    void eliminarTokensExpiradosOInactivos(@Param("fechaLimite") LocalDateTime fechaLimite);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.usuario = :usuario AND rt.activo = true")
    int contarTokensActivosDelUsuario(@Param("usuario") Usuario usuario);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.activo = false WHERE rt.token = :token")
    void desactivarToken(@Param("token") String token);

    // Método adicional para encontrar tokens por usuario (útil para debugging)
    List<RefreshToken> findByUsuario(Usuario usuario);
}