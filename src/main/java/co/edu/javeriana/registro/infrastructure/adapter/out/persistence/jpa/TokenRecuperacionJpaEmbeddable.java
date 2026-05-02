package co.edu.javeriana.registro.infrastructure.adapter.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class TokenRecuperacionJpaEmbeddable {

    @Column(name = "recovery_token")
    private String token;

    @Column(name = "recovery_expiration")
    private LocalDateTime fechaExpiracion;

    @Column(name = "recovery_used")
    private boolean usado;

    public TokenRecuperacionJpaEmbeddable() {
    }

    public TokenRecuperacionJpaEmbeddable(String token, LocalDateTime fechaExpiracion, boolean usado) {
        this.token = token;
        this.fechaExpiracion = fechaExpiracion;
        this.usado = usado;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }

    public void setUsado(boolean usado) {
        this.usado = usado;
    }
}
