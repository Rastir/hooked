package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ActualizarComentarioRequest {

    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 1, max = 1000, message = "El comentario debe tener entre 1 y 1000 caracteres")
    private String contenido;

    public ActualizarComentarioRequest() {}

    public ActualizarComentarioRequest(String contenido) {
        this.contenido = contenido;
    }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}