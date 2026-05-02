package co.edu.javeriana.registro.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class TokenRecuperacion {
    private final String token;
    private final LocalDateTime fechaExpiracion;
    private boolean usado;

    public TokenRecuperacion(String token, LocalDateTime fechaExpiracion) {
        this.token = Objects.requireNonNull(token, "El token no puede ser nulo");
        this.fechaExpiracion = Objects.requireNonNull(fechaExpiracion, "La fecha de expiración no puede ser nula");
        this.usado = false;
    }

    public TokenRecuperacion(String token, LocalDateTime fechaExpiracion, boolean usado) {
        this(token, fechaExpiracion);
        this.usado = usado;
    }

    public boolean esValido(LocalDateTime fechaActual) {
        return !usado && fechaActual.isBefore(fechaExpiracion);
    }

    public void marcarComoUsado() {
        this.usado = true;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public boolean isUsado() {
        return usado;
    }
}
