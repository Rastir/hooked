package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.Size;

public class ActualizarPostRequest {

    @Size(min = 5, max = 200, message = "El titulo debe tener entre 5 y 200 caracteres")
    private String titulo;

    @Size(min = 10, message = "El contenido debe tener al menos 10 caracteres")
    private String contenido;

    private String fotoLink;

    private Long categoriaId;

    // Getters y setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getFotoLink() {
        return fotoLink;
    }

    public void setFotoLink(String fotoLink) {
        this.fotoLink = fotoLink;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
}