package com.flaco.hooked.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts", indexes = {

        // Lista principal ordenada por fecha
        @Index(name = "idx_post_fecha_creacion", columnList = "fechaCreacion DESC"),

        // Filtros + ordenamiento
        @Index(name = "idx_post_usuario_fecha", columnList = "usuario_id, fechaCreacion DESC"),
        @Index(name = "idx_post_categoria_fecha", columnList = "categoria_id, fechaCreacion DESC"),
        @Index(name = "idx_post_usuario_categoria_fecha", columnList = "usuario_id, categoria_id, fechaCreacion DESC"),

        // INDICE PARA POSTS POPULARES
        @Index(name = "idx_post_likes", columnList = "likeCount DESC"),

        // INDICE PARA BÚSQUEDAS
        @Index(name = "idx_post_titulo", columnList = "titulo"),

        // INDICES PARA ESTADÍSTICAS
        @Index(name = "idx_post_usuario_stats", columnList = "usuario_id"),
        @Index(name = "idx_post_categoria_stats", columnList = "categoria_id")
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private String fotoLink;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private Integer likeCount = 0;

    // Relación de quien crea el post
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Relación con categoria
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    @JsonBackReference
    private Categoria categoria;

    // Relación con los comentarios del post
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comentario> comentarios = new ArrayList<>();

    // (constructores, getters, setters, métodos helper, etc.)

    //CONSTRUCTORES
    public Post() {
    }

    public Post(Long id, String titulo, String contenido, String fotoLink, LocalDateTime fechaCreacion, Integer likeCount, Usuario usuario, Categoria categoria, List<Comentario> comentarios) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fotoLink = fotoLink;
        this.fechaCreacion = fechaCreacion;
        this.likeCount = likeCount;
        this.usuario = usuario;
        this.categoria = categoria;
        this.comentarios = comentarios;
    }

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
        if (likeCount == null) {
            likeCount = 0;
        }
    }

    //GETTERS
    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public String getFotoLink() {
        return fotoLink;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Categoria getCategoria() {
        return categoria;
    }
    public List<Comentario> getComentarios() {
        return comentarios;
    }

    //SETTERS
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public void setFotoLink(String fotoLink) {
        this.fotoLink = fotoLink;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    // Metodos de like
    public void incrementarLikes() {
        this.likeCount = (this.likeCount != null ? this.likeCount : 0) + 1;
    }

    public void decrementarLikes() {
        this.likeCount = Math.max(0, (this.likeCount != null ? this.likeCount : 0) - 1);
    }

    // Método helper para agregar comentario
    public void agregarComentario(Comentario comentario) {
        comentarios.add(comentario);
        comentario.setPost(this);
    }

    // Método helper para remover comentario
    public void removerComentario(Comentario comentario) {
        comentarios.remove(comentario);
        comentario.setPost(null);
    }



    // EQUALS Y HASHCODE (ID)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return id != null && id.equals(post.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}