package com.flaco.hooked.domain.response;


public class LoginResponse {

    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String email;
    private String nombre;

    public LoginResponse() {}

    public LoginResponse(String token) {
        this.token = token;
    }

    // Constructor completo
    public LoginResponse(String token, Long id, String email, String nombre) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.nombre = nombre;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}