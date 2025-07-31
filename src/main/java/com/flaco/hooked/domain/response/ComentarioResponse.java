package com.flaco.hooked.domain.response;

import com.flaco.hooked.model.Comentario;
import java.time.LocalDateTime;

public class ComentarioResponse {

    private Long id;
    private String contenido;
    private AutorResponse autor;
    private Long comentarioPadreId; // 👈 AGREGAR ESTA LÍNEA

    // Constructor desde entidad
    public ComentarioResponse(Comentario comentario) {
        this.id = comentario.getId();
        this.contenido = comentario.getContenido();
        this.autor = new AutorResponse(comentario.getUsuario());
        this.comentarioPadreId = comentario.getComentarioPadre() != null ?
                comentario.getComentarioPadre().getId() : null; // 👈 Y ESTA LÓGICA
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public AutorResponse getAutor() { return autor; }
    public void setAutor(AutorResponse autor) { this.autor = autor; }

    public Long getComentarioPadreId() { return comentarioPadreId; } // 👈 GETTER
    public void setComentarioPadreId(Long comentarioPadreId) { this.comentarioPadreId = comentarioPadreId; } // 👈 SETTER

    // Clase interna AutorResponse (igual que antes)
    public static class AutorResponse {
        private Long id;
        private String nombre;
        private String fotoPerfil;

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