package com.flaco.hooked.domain.categoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT c FROM Categoria c WHERE SIZE(c.posts) > 0")
    List<Categoria> findCategoriasWithPosts();
}