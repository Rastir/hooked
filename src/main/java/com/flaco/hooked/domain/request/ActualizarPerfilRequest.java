package com.flaco.hooked.domain.request;

import jakarta.validation.constraints.*;
import java.util.List;

public class ActualizarPerfilRequest {

    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 255, message = "El email no puede exceder 255 caracteres")
    private String email;

    @Size(max = 500, message = "La bio no puede exceder 500 caracteres")
    private String bio;

    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    private String ubicacionPreferida;

    @Size(max = 10, message = "Máximo 10 tags permitidos")
    private List<@Size(max = 20, message = "Cada tag no puede exceder 20 caracteres") String> tags;

    @Size(min = 6, max = 100, message = "La nueva contraseña debe tener entre 6 y 100 caracteres")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).*$",
            message = "La contraseña debe contener al menos una letra y un número"
    )
    private String nuevaContrasena;

    private String contrasenaActual;

    public ActualizarPerfilRequest() {}

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

    // Getters y setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getUbicacionPreferida() { return ubicacionPreferida; }
    public void setUbicacionPreferida(String ubicacionPreferida) { this.ubicacionPreferida = ubicacionPreferida; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getNuevaContrasena() { return nuevaContrasena; }
    public void setNuevaContrasena(String nuevaContrasena) { this.nuevaContrasena = nuevaContrasena; }

    public String getContrasenaActual() { return contrasenaActual; }
    public void setContrasenaActual(String contrasenaActual) { this.contrasenaActual = contrasenaActual; }

    // Helpers
    public boolean tieneNuevaContrasena() {
        return nuevaContrasena != null && !nuevaContrasena.trim().isEmpty();
    }

    public boolean tieneContrasenaActual() {
        return contrasenaActual != null && !contrasenaActual.trim().isEmpty();
    }

    public boolean esCambioDeContrasena() {
        return tieneNuevaContrasena() && tieneContrasenaActual();
    }

    public boolean tieneCambios() {
        return nombre != null || email != null || bio != null ||
                ubicacionPreferida != null || tags != null ||
                nuevaContrasena != null;
    }
}