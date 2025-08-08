package com.flaco.hooked.domain.refreshtoken;


import com.flaco.hooked.model.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_refresh_token", columnList = "token", unique = true),
        @Index(name = "idx_refresh_usuario_activo", columnList = "usuario_id, activo"),
        @Index(name = "idx_refresh_expiracion", columnList = "fechaExpiracion"),
        @Index(name = "idx_refresh_usuario_fecha", columnList = "usuario_id, fechaCreacion DESC")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(length = 500)
    private String dispositivoInfo; // User-Agent info

    @Column(length = 45)
    private String ipAddress;

    // Constructores
    public RefreshToken() {}

    public RefreshToken(String token, Usuario usuario, LocalDateTime fechaCreacion,
                        LocalDateTime fechaExpiracion, String dispositivoInfo, String ipAddress) {
        this.token = token;
        this.usuario = usuario;
        this.fechaCreacion = fechaCreacion;
        this.fechaExpiracion = fechaExpiracion;
        this.dispositivoInfo = dispositivoInfo;
        this.ipAddress = ipAddress;
        this.activo = true;
    }

    // Getters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public Usuario getUsuario() { return usuario; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public Boolean getActivo() { return activo; }
    public String getDispositivoInfo() { return dispositivoInfo; }
    public String getIpAddress() { return ipAddress; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setToken(String token) { this.token = token; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public void setDispositivoInfo(String dispositivoInfo) { this.dispositivoInfo = dispositivoInfo; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    // Métodos de utilidad
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }

    public boolean isExpirado() {
        return fechaExpiracion.isBefore(LocalDateTime.now());
    }

    public boolean isActivo() {
        return activo != null && activo;
    }

    // toString, equals y hashCode
    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaExpiracion=" + fechaExpiracion +
                ", activo=" + activo +
                ", dispositivoInfo='" + dispositivoInfo + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}