package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.*;

public class CrearComentarioRequest {

    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 1, max = 1000, message = "El comentario debe tener entre 1 y 1000 caracteres")
    private String contenido;

    @NotNull(message = "El post es obligatorio")
    @Positive(message = "El ID del post debe ser válido")
    private Long postId;

    @Positive(message = "El ID del comentario padre debe ser válido")
    private Long comentarioPadreId; // null = comentario principal

    // Constructor vacío
    public CrearComentarioRequest() {}

    // Constructor para comentario principal
    public CrearComentarioRequest(String contenido, Long postId) {
        this.contenido = contenido;
        this.postId = postId;
    }

    // Constructor para respuesta
    public CrearComentarioRequest(String contenido, Long postId, Long comentarioPadreId) {
        this.contenido = contenido;
        this.postId = postId;
        this.comentarioPadreId = comentarioPadreId;
    }

    // Getters y setters
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getComentarioPadreId() { return comentarioPadreId; }
    public void setComentarioPadreId(Long comentarioPadreId) { this.comentarioPadreId = comentarioPadreId; }

    // Helper
    public boolean esRespuesta() {
        return comentarioPadreId != null;
    }
}