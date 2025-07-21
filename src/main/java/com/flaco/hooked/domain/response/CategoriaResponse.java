package com.flaco.hooked.domain.response;


public class CategoriaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Integer totalPosts;

    // Constructor vacío
    public CategoriaResponse() {}

    // Constructor con parámetros
    public CategoriaResponse(Long id, String nombre, String descripcion, Integer totalPosts) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.totalPosts = totalPosts;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Integer totalPosts) {
        this.totalPosts = totalPosts;
    }
}