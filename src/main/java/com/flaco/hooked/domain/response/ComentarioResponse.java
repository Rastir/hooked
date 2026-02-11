package com.flaco.hooked.domain.response;

import com.flaco.hooked.model.Comentario;
import java.time.LocalDateTime;

public class ComentarioResponse {

    private Long id;
    private String contenido;
    private LocalDateTime fechaCreacion;
    private AutorResponse autor;
    private Long comentarioPadreId;
    private Long postId;
    private Integer totalRespuestas;

    public ComentarioResponse() {}

    public ComentarioResponse(Comentario comentario) {
        this.id = comentario.getId();
        this.contenido = comentario.getContenido();
        this.fechaCreacion = comentario.getFechaCreacion();
        this.autor = new AutorResponse(comentario.getUsuario());
        this.comentarioPadreId = comentario.getComentarioPadre() != null
                ? comentario.getComentarioPadre().getId()
                : null;
        this.postId = comentario.getPost() != null
                ? comentario.getPost().getId()
                : null;
        this.totalRespuestas = comentario.getRespuestas() != null
                ? comentario.getRespuestas().size()
                : 0;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public AutorResponse getAutor() { return autor; }
    public void setAutor(AutorResponse autor) { this.autor = autor; }

    public Long getComentarioPadreId() { return comentarioPadreId; }
    public void setComentarioPadreId(Long comentarioPadreId) { this.comentarioPadreId = comentarioPadreId; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Integer getTotalRespuestas() { return totalRespuestas; }
    public void setTotalRespuestas(Integer totalRespuestas) { this.totalRespuestas = totalRespuestas; }

    // Helpers
    public boolean esRespuesta() {
        return comentarioPadreId != null;
    }

    // Clase interna
    public static class AutorResponse {
        private Long id;
        private String nombre;
        private String fotoPerfil;

        public AutorResponse() {}

        public AutorResponse(com.flaco.hooked.model.Usuario usuario) {
            this.id = usuario.getId();
            this.nombre = usuario.getNombre();
            this.fotoPerfil = usuario.getFotoPerfil();
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getFotoPerfil() { return fotoPerfil; }
        public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
    }
}