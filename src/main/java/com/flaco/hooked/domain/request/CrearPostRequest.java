package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.*;

public class CrearPostRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
    private String titulo;

    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 10, max = 5000, message = "El contenido debe tener entre 10 y 5000 caracteres")
    private String contenido;

    @Size(max = 1000, message = "El link de la foto no puede exceder 1000 caracteres")
    @Pattern(
            regexp = "^(https?://.*|)$",
            message = "El link de la foto debe ser una URL válida o estar vacío"
    )
    private String fotoLink;

    @NotNull(message = "La categoría es obligatoria")
    @Positive(message = "El ID de categoría debe ser válido")
    private Long categoriaId;

    // Constructor vacío
    public CrearPostRequest() {}

    // Getters y setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getFotoLink() { return fotoLink; }
    public void setFotoLink(String fotoLink) { this.fotoLink = fotoLink; }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
}