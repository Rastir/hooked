package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.*;
import java.util.List;

public class ActualizarPerfilRequest {

    // DATOS BÁSICOS
    @Size(min=2, max=100, message="Nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Email(message="Email debe ser válido")
    private String email;

    // NUEVOS CAMPOS DE PERFIL
    @Size(max=500, message="Bio no puede exceder 500 caracteres")
    private String bio;

    @Size(max=100, message="Ubicación no puede exceder 100 caracteres")
    private String ubicacionPreferida;

    private List<@Size(max=20, message="Tag no puede exceder 20 caracteres") String> tags;

    //  CAMBIO DE CONTRASEÑA
    @Size(min=6, message="Nueva contraseña debe tener al menos 6 caracteres")
    private String nuevaContrasena;

    @Size(min=6, message="Contraseña actual requerida para cambios")
    private String contrasenaActual;

    // CONSTRUCTORES
    public ActualizarPerfilRequest() {
    }

    public ActualizarPerfilRequest(String nombre, String email, String bio,
                                   String ubicacionPreferida, List<String> tags,
                                   String nuevaContrasena, String contrasenaActual) {
        this.nombre = nombre;
        this.email = email;
        this.bio = bio;
        this.ubicacionPreferida = ubicacionPreferida;
        this.tags = tags;
        this.nuevaContrasena = nuevaContrasena;
        this.contrasenaActual = contrasenaActual;
    }

    //GETTERS
    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getBio() {
        return bio;
    }

    public String getUbicacionPreferida() {
        return ubicacionPreferida;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    // SETTERS
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setUbicacionPreferida(String ubicacionPreferida) {
        this.ubicacionPreferida = ubicacionPreferida;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }

    public void setContrasenaActual(String contrasenaActual) {
        this.contrasenaActual = contrasenaActual;
    }

    //CAMBIO DE CONTRASEÑA
    public boolean tieneNuevaContrasena() {
        return nuevaContrasena != null && !nuevaContrasena.trim().isEmpty();
    }

    public boolean tieneContrasenaActual() {
        return contrasenaActual != null && !contrasenaActual.trim().isEmpty();
    }

    public boolean esCambioDeContrasena() {
        return tieneNuevaContrasena() && tieneContrasenaActual();
    }
}