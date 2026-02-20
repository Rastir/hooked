package com.flaco.hooked.domain.response;

import com.flaco.hooked.model.Usuario;
import java.time.LocalDateTime;
import java.util.List;

public class UsuarioResponse {

    private Long id;
    private String nombre;
    private String email;
    private LocalDateTime fechaRegistro;

    private String fotoPerfil;
    private String bio;
    private List<String> tags;
    private String ubicacionPreferida;

    private Integer totalPosts;
    private Integer totalLikes;
    private Integer totalComentarios;

    private LocalDateTime ultimaActividad;
    private String nivelPescador;

    public UsuarioResponse() {}

    public UsuarioResponse(Long id, String nombre, String email, LocalDateTime fechaRegistro,
                           String fotoPerfil, String bio, List<String> tags, String ubicacionPreferida,
                           Integer totalPosts, Integer totalLikes, Integer totalComentarios,
                           LocalDateTime ultimaActividad, String nivelPescador) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.fotoPerfil = fotoPerfil;
        this.bio = bio;
        this.tags = tags;
        this.ubicacionPreferida = ubicacionPreferida;
        this.totalPosts = totalPosts;
        this.totalLikes = totalLikes;
        this.totalComentarios = totalComentarios;
        this.ultimaActividad = ultimaActividad;
        this.nivelPescador = nivelPescador;
    }

    public UsuarioResponse(Usuario usuario) {
        this(usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.getFechaRegistro(), usuario.getFotoPerfil(), usuario.getBio(),
                usuario.getTags(), usuario.getUbicacionPreferida(), null, null, null,
                usuario.getUltimaActividad(), usuario.getNivelPescador());
    }

    // Getters y setters (todos igual)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getUbicacionPreferida() { return ubicacionPreferida; }
    public void setUbicacionPreferida(String ubicacionPreferida) { this.ubicacionPreferida = ubicacionPreferida; }

    public Integer getTotalPosts() { return totalPosts; }
    public void setTotalPosts(Integer totalPosts) { this.totalPosts = totalPosts; }

    public Integer getTotalLikes() { return totalLikes; }
    public void setTotalLikes(Integer totalLikes) { this.totalLikes = totalLikes; }

    public Integer getTotalComentarios() { return totalComentarios; }
    public void setTotalComentarios(Integer totalComentarios) { this.totalComentarios = totalComentarios; }

    public LocalDateTime getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(LocalDateTime ultimaActividad) { this.ultimaActividad = ultimaActividad; }

    public String getNivelPescador() { return nivelPescador; }
    public void setNivelPescador(String nivelPescador) { this.nivelPescador = nivelPescador; }

    // Helper
    public boolean tieneFotoPerfil() {
        return fotoPerfil != null && !fotoPerfil.isEmpty();
    }

    public boolean tieneBio() {
        return bio != null && !bio.isEmpty();
    }

    public boolean tieneUbicacion() {
        return ubicacionPreferida != null && !ubicacionPreferida.isEmpty();
    }

    public boolean tieneTags() {
        return tags != null && !tags.isEmpty();
    }
}