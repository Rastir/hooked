package com.flaco.hooked.domain.response;

import com.flaco.hooked.model.Post;
import java.time.LocalDateTime;

public class PostResponse {
    private Long id;
    private String titulo;
    private String contenido;
    private String fotoLink;
    private LocalDateTime fechaCreacion;
    private Integer likeCount;
    private UsuarioResponse autor;
    private CategoriaResponse categoria;
    private Long comentariosCount;

    public PostResponse() {}

    public PostResponse(Long id, String titulo, String contenido, String fotoLink,
                        LocalDateTime fechaCreacion, Integer likeCount,
                        UsuarioResponse autor, CategoriaResponse categoria,
                        Long comentariosCount) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fotoLink = fotoLink;
        this.fechaCreacion = fechaCreacion;
        this.likeCount = likeCount;
        this.autor = autor;
        this.categoria = categoria;
        this.comentariosCount = comentariosCount;
    }

    // Constructor desde entidad
    public PostResponse(Post post) {
        this.id = post.getId();
        this.titulo = post.getTitulo();
        this.contenido = post.getContenido();
        this.fotoLink = post.getFotoLink();
        this.fechaCreacion = post.getFechaCreacion();
        this.likeCount = post.getLikeCount();
        this.autor = new UsuarioResponse(post.getUsuario());
        this.categoria = new CategoriaResponse(post.getCategoria());
        this.comentariosCount = post.getComentarios() != null
                ? (long) post.getComentarios().size()
                : 0L;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public String getFotoLink() { return fotoLink; }
    public void setFotoLink(String fotoLink) { this.fotoLink = fotoLink; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public UsuarioResponse getAutor() { return autor; }
    public void setAutor(UsuarioResponse autor) { this.autor = autor; }

    public CategoriaResponse getCategoria() { return categoria; }
    public void setCategoria(CategoriaResponse categoria) { this.categoria = categoria; }

    public Long getComentariosCount() { return comentariosCount; }
    public void setComentariosCount(Long comentariosCount) { this.comentariosCount = comentariosCount; }

    // Clases internas
    public static class UsuarioResponse {
        private Long id;
        private String nombre;
        private String email;

        public UsuarioResponse() {}

        public UsuarioResponse(com.flaco.hooked.model.Usuario usuario) {
            this.id = usuario.getId();
            this.nombre = usuario.getNombre();
            this.email = usuario.getEmail();
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class CategoriaResponse {
        private Long id;
        private String nombre;

        public CategoriaResponse() {}

        public CategoriaResponse(com.flaco.hooked.model.Categoria categoria) {
            this.id = categoria.getId();
            this.nombre = categoria.getNombre();
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }
}