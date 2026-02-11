package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.*;

public class ActualizarPostRequest {

    @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
    private String titulo;

    @Size(min = 10, max = 5000, message = "El contenido debe tener entre 10 y 5000 caracteres")
    private String contenido;

    @Size(max = 1000, message = "El link de la foto no puede exceder 1000 caracteres")
    @Pattern(
            regexp = "^(https?://.*|)$",
            message = "El link de la foto debe ser una URL válida o estar vacío"
    )
    private String fotoLink;

    @Positive(message = "El ID de categoría debe ser válido")
    private Long categoriaId;

    // Constructor vacío
    public ActualizarPostRequest() {}

    // Constructor completo
    public ActualizarPostRequest(String titulo, String contenido, String fotoLink, Long categoriaId) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.fotoLink = fotoLink;
        this.categoriaId = categoriaId;
    }

    // Getters y setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getFotoLink() { return fotoLink; }
    public void setFotoLink(String fotoLink) { this.fotoLink = fotoLink; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    // Helper para verificar si hay cambios
    public boolean tieneCambios() {
        return titulo != null || contenido != null || fotoLink != null || categoriaId != null;
    }
}