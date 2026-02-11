package com.flaco.hooked.domain.response;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tipo = "Bearer";
    private Long expiresIn;
    private Long id;
    private String email;
    private String nombre;

    public LoginResponse() {}

    // Constructor completo
    public LoginResponse(String accessToken, String refreshToken, Long expiresIn,
                         Long id, String email, String nombre) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.id = id;
        this.email = email;
        this.nombre = nombre;
    }

    // Getters y setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return "LoginResponse{id=" + id + ", email='" + email + "', nombre='" + nombre + "'}";
    }
}