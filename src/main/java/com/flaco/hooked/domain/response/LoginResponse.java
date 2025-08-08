package com.flaco.hooked.domain.response;

public class LoginResponse {

    private String token;
    private String refreshToken;
    private String tipo = "Bearer";
    private Long expiresIn;
    private Long id;
    private String email;
    private String nombre;

    public LoginResponse() {}

    public LoginResponse(String token) {
        this.token = token;
    }

    // Constructor completo original
    public LoginResponse(String token, Long id, String email, String nombre) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.nombre = nombre;
    }

    // Constructor completo con refresh token
    public LoginResponse(String token, String refreshToken, Long expiresIn,
                         Long id, String email, String nombre) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
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

    // Getters y Setters para refresh token
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='" + token + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", tipo='" + tipo + '\'' +
                ", expiresIn=" + expiresIn +
                ", id=" + id +
                ", email='" + email + '\'' +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}