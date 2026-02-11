package com.flaco.hooked.domain.response;

import com.flaco.hooked.model.Categoria;

public class CategoriaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer totalPosts;

    public CategoriaResponse() {}

    public CategoriaResponse(Long id, String nombre, String descripcion, Integer totalPosts) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.totalPosts = totalPosts;
    }

    public CategoriaResponse(Categoria categoria) {
        this.id = categoria.getId();
        this.nombre = categoria.getNombre();
        this.descripcion = categoria.getDescripcion();
        this.totalPosts = categoria.getPosts() != null ? categoria.getPosts().size() : 0;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getTotalPosts() { return totalPosts; }
    public void setTotalPosts(Integer totalPosts) { this.totalPosts = totalPosts; }
}