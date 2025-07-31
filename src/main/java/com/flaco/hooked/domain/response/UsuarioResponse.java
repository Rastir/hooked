package com.flaco.hooked.domain.response;

import com.flaco.hooked.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public class UsuarioResponse {

    //DATOS BÁSICOS (seguros)
    private Long id;
    private String nombre;
    private String email;
    private LocalDateTime fechaRegistro;

    // NUEVAS FUNCIONALIDADES DE PERFIL <-- importante
    private String fotoPerfil;           // URL/path de la foto
    private String bio;                  // Historia del pescador
    private List<String> tags;           // ["Pesca nocturna", "Experto en robalo"]
    private String ubicacionPreferida;   // "Cancún, Quintana Roo"

    // ESTADÍSTICAS
    private Integer totalPosts;          // Cantidad de posts del usuario
    private Integer totalLikes;          // Likes recibidos en todos sus posts
    private Integer totalComentarios;    // Para futuro sistema de comentarios

    // DATOS ESPECÍFICOS DE PESCADOR (para futuro)
    private LocalDateTime ultimaActividad;  // Último post/like/comentario
    private String nivelPescador;           // "Principiante", "Intermedio", "Experto"


    public UsuarioResponse() {
    }

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

    // CONSTRUCTOR DESDE ENTIDAD
    public UsuarioResponse(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.fechaRegistro = usuario.getFechaRegistro();
        this.fotoPerfil = usuario.getFotoPerfil();
        this.bio = usuario.getBio();
        this.tags = usuario.getTags();
        this.ubicacionPreferida = usuario.getUbicacionPreferida();
        this.ultimaActividad = usuario.getUltimaActividad();
        this.nivelPescador = usuario.getNivelPescador();

        // Estadísticas se dejan null para este constructor
        this.totalPosts = null;
        this.totalLikes = null;
        this.totalComentarios = null;
    }


    // GETTERS
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public String getBio() {
        return bio;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getUbicacionPreferida() {
        return ubicacionPreferida;
    }

    public Integer getTotalPosts() {
        return totalPosts;
    }

    public Integer getTotalLikes() {
        return totalLikes;
    }

    public Integer getTotalComentarios() {
        return totalComentarios;
    }

    public LocalDateTime getUltimaActividad() {
        return ultimaActividad;
    }

    public String getNivelPescador() {
        return nivelPescador;
    }

    // SETTERS
    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setUbicacionPreferida(String ubicacionPreferida) {
        this.ubicacionPreferida = ubicacionPreferida;
    }

    public void setTotalPosts(Integer totalPosts) {
        this.totalPosts = totalPosts;
    }

    public void setTotalLikes(Integer totalLikes) {
        this.totalLikes = totalLikes;
    }

    public void setTotalComentarios(Integer totalComentarios) {
        this.totalComentarios = totalComentarios;
    }

    public void setUltimaActividad(LocalDateTime ultimaActividad) {
        this.ultimaActividad = ultimaActividad;
    }

    public void setNivelPescador(String nivelPescador) {
        this.nivelPescador = nivelPescador;
    }
}