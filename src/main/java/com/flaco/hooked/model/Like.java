package com.flaco.hooked.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flaco.hooked.model.Post;
import com.flaco.hooked.model.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "post_id"}))
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;

    @Column(name = "fecha_like")
    private LocalDateTime fechaLike;

    @PrePersist
    protected void onCreate() {
        fechaLike = LocalDateTime.now();
    }

    // CONSTRUCTORES
    public Like() {}

    public Like(Usuario usuario, Post post) {
        this.usuario = usuario;
        this.post = post;
    }

    // GETTERS Y SETTERS (sin @JsonIgnore aquí)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public LocalDateTime getFechaLike() { return fechaLike; }
    public void setFechaLike(LocalDateTime fechaLike) { this.fechaLike = fechaLike; }
}