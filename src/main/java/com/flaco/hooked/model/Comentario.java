package com.flaco.hooked.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios", indexes = {
        // Comentarios por post ordenados por fecha
        @Index(name = "idx_comentario_post_fecha", columnList = "post_id, fecha_creacion ASC"),

        // Comentarios principales (sin padre)
        @Index(name = "idx_comentario_post_principal", columnList = "post_id, comentario_padre_id, fecha_creacion ASC"),

        // INDICE PARA RESPUESTAS ANIDADAS
        @Index(name = "idx_comentario_padre_fecha", columnList = "comentario_padre_id, fecha_creacion ASC"),

        // INDICE PARA COMENTARIOS POR USUARIO
        @Index(name = "idx_comentario_usuario_fecha", columnList = "usuario_id, fecha_creacion DESC"),

        // INDICES PARA ESTADÍSTICAS Y CONTEOS
        @Index(name = "idx_comentario_post_count", columnList = "post_id"),
        @Index(name = "idx_comentario_usuario_count", columnList = "usuario_id"),

        // INDICE PARA COMENTARIOS PADRE (verificar si tiene respuestas)
        @Index(name = "idx_comentario_padre", columnList = "comentario_padre_id")
})
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String contenido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // Para respuestas a comentarios (v2 - opcional por ahora)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comentario_padre_id")
    private Comentario comentarioPadre;

    // Constructores
    public Comentario() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Comentario(String contenido, Usuario usuario, Post post) {
        this();
        this.contenido = contenido;
        this.usuario = usuario;
        this.post = post;
    }

    // GETTERS Y SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public Comentario getComentarioPadre() { return comentarioPadre; }
    public void setComentarioPadre(Comentario comentarioPadre) { this.comentarioPadre = comentarioPadre; }
}