package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CrearComentarioRequest {

    @NotBlank(message = "El contenido no puede estar vacío")
    @Size(min = 1, max = 1000, message = "El contenido debe tener entre 1 y 1000 caracteres")
    private String contenido;

    @NotNull(message = "El ID del post es obligatorio")
    private Long postId;

    // Para respuestas (opcional v2)
    private Long comentarioPadreId;

    // Constructores
    public CrearComentarioRequest() {}

    public CrearComentarioRequest(String contenido, Long postId) {
        this.contenido = contenido;
        this.postId = postId;
    }

    // Getters y Setters
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getComentarioPadreId() { return comentarioPadreId; }
    public void setComentarioPadreId(Long comentarioPadreId) { this.comentarioPadreId = comentarioPadreId; }
}