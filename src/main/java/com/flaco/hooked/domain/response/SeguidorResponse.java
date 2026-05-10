package com.flaco.hooked.domain.response;

import com.flaco.hooked.model.Usuario;
public class SeguidorResponse {
    private Long id;
    private String nombre;
    private String fotoPerfil;
    private String nivelPescador;
    private boolean esFishingBuddy;
    private boolean yoLoSigo;

    public SeguidorResponse() {}

    public SeguidorResponse(Usuario usuario, boolean esFishingBuddy, boolean yoLoSigo) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.fotoPerfil = usuario.getFotoPerfil();
        this.nivelPescador = usuario.getNivelPescador();
        this.esFishingBuddy = esFishingBuddy;
        this.yoLoSigo = yoLoSigo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public String getNivelPescador() { return nivelPescador; }
    public void setNivelPescador(String nivelPescador) { this.nivelPescador = nivelPescador; }

    public boolean isEsFishingBuddy() { return esFishingBuddy; }
    public void setEsFishingBuddy(boolean esFishingBuddy) { this.esFishingBuddy = esFishingBuddy; }

    public boolean isYoLoSigo() { return yoLoSigo; }
    public void setYoLoSigo(boolean yoLoSigo) { this.yoLoSigo = yoLoSigo; }
}
