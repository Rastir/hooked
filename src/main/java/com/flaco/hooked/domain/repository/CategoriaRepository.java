package com.flaco.hooked.domain.repository;

import com.flaco.hooked.model.Categoria;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    @EntityGraph(attributePaths = {"posts"})
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    // Optimizado: evita cargar todos los posts solo para contar
    @Query("SELECT c FROM Categoria c WHERE (SELECT COUNT(p) FROM Post p WHERE p.categoria = c) > 0")
    List<Categoria> findCategoriasWithPosts();

    // Alternativa más eficiente si solo necesitas IDs/nombres
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.posts p")
    @EntityGraph(attributePaths = {"posts"})
    List<Categoria> findCategoriasConPostsCargados();

    // Para edición (carga posts)
    @EntityGraph(attributePaths = {"posts"})
    Optional<Categoria> findWithPostsById(Long id);
}