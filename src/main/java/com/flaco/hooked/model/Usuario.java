package com.flaco.hooked.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios", indexes = {
        // Indice para un login rápido
        @Index(name = "idx_usuario_email", columnList = "email", unique = true),

        // Indices para busqueda avanzada - Multicamp
        @Index(name = "idx_usuario_nombre", columnList = "nombre"),
        @Index(name = "idx_usuario_ubicacion", columnList = "ubicacion_preferida"),
        //Pendiente de agregar un index para los tags
        // este está roto -- >  @Index(name = "idx_usuario_tags", columnList = "tags"),
        @Index(name = "idx_usuario_nivel", columnList = "nivel_pescador"),

        //Ordenamiento y filtros por fecha
        @Index(name = "idx_usuario_fecha_registro", columnList = "fecha_registro DESC"),
        @Index(name = "idx_usuario_ultima_actividad", columnList = "ultima_actividad DESC"),

        // Combinaciones frecuentes
        @Index(name = "idx_usuario_nivel_fecha", columnList = "nivel_pescador, fecha_registro DESC"),
        @Index(name = "idx_usuario_ubicacion_fecha", columnList = "ubicacion_preferida, fecha_registro DESC"),
        @Index(name = "idx_usuario_actividad_fecha", columnList = "ultima_actividad, fecha_registro DESC"),

        // Indice para usuarios más activos (optimiza el GROUP BY)
        @Index(name = "idx_usuario_posts_count", columnList = "id, fecha_registro DESC")
})
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String contrasena;

    //CAMPOS DE PERFIL
    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "ubicacion_preferida", length = 100)
    private String ubicacionPreferida;

    @Column(name = "tags", length = 1000)
    private String tagsString;

    // ESTADISTICA
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "ultima_actividad")
    private LocalDateTime ultimaActividad;

    @Column(name = "nivel_pescador", length = 20)
    private String nivelPescador;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Post> posts = new ArrayList<>();

    // CONSTRUCTORES
    public Usuario() {
        this.fechaRegistro = LocalDateTime.now();
        this.ultimaActividad = LocalDateTime.now();
        this.nivelPescador = "Principiante"; // Valor por defecto
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return contrasena;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    //GETTERS Y SETTERS ORIGINALES
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUbicacionPreferida() {
        return ubicacionPreferida;
    }

    public void setUbicacionPreferida(String ubicacionPreferida) {
        this.ubicacionPreferida = ubicacionPreferida;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getUltimaActividad() {
        return ultimaActividad;
    }

    public void setUltimaActividad(LocalDateTime ultimaActividad) {
        this.ultimaActividad = ultimaActividad;
    }

    public String getNivelPescador() {
        return nivelPescador;
    }

    public void setNivelPescador(String nivelPescador) {
        this.nivelPescador = nivelPescador;
    }

    // 🎯 MÉTODOS HELPER PARA TAGS (la magia aquí)
    public List<String> getTags() {
        if (tagsString == null || tagsString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(tagsString.split(","));
    }

    public void setTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            this.tagsString = null;
        } else {
            // Limpiar espacios y filtrar vacíos
            this.tagsString = String.join(",",
                    tags.stream()
                            .map(String::trim)
                            .filter(tag -> !tag.isEmpty())
                            .toArray(String[]::new)
            );
        }
    }

    public void addPost(Post post) {
        posts.add(post);
        post.setUsuario(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setUsuario(null);
    }

    // Getter/Setter para el campo String interno (para JPA)
    public String getTagsString() {
        return tagsString;
    }

    public void setTagsString(String tagsString) {
        this.tagsString = tagsString;
    }

    // MÉTODOS DE EXTRA
    public void actualizarUltimaActividad() {
        this.ultimaActividad = LocalDateTime.now();
    }

    public boolean tieneFotoPerfil() {
        return fotoPerfil != null && !fotoPerfil.trim().isEmpty();
    }

    public boolean tieneBio() {
        return bio != null && !bio.trim().isEmpty();
    }

    public boolean tieneUbicacionPreferida() {
        return ubicacionPreferida != null && !ubicacionPreferida.trim().isEmpty();
    }
}