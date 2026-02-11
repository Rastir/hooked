package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CrearCategoriaRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    // Constructor vacío requerido por Spring
    public CrearCategoriaRequest() {}

    // Constructor completo
    public CrearCategoriaRequest(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}