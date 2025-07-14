package com.flaco.hooked.domain.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CrearCategoriaRequest {

    @NotBlank(message = "El nombre de la categoria es obligatorio")
    @Size(min = 3,max = 100,message = "El nombre debe de tener entre 3 y 100 caracteres")
    private String nombre;

    @Size(max = 1000,message = "La entrada no puede tener más de 1000 caracteres")
    private String descripcion;

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
}
