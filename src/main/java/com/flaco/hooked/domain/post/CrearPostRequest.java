package com.flaco.hooked.domain.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CrearPostRequest {

    @NotBlank
    @Size(min = 5, max = 200, message = "El titulo debe tener entre 5 y 200 caracteres")
    private String titulo;

    @NotBlank(message = "El contenido no puede estar en blanco")
    private String contenido;
    private String fotoLink;

    @NotNull(message = "El Id del usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El id de la categoría es obligatorio")
    private Long categoriaId;

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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
}
