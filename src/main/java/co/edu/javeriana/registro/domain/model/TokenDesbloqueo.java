package co.edu.javeriana.registro.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que representa un token de desbloqueo de cuenta.
 * Implementa las reglas RN-AUTH-016 a RN-AUTH-019 de HU-AUTH-005.
 * 
 * <ul>
 *   <li>Vigencia: 1 hora (configurable)</li>
 *   <li>Un solo uso</li>
 *   <li>Revocable (al solicitar reenvío)</li>
 * </ul>
 */
public final class TokenDesbloqueo {

    private static final long HORAS_VIGENCIA = 1;

    private final String token;
    private final String userId;
    private final LocalDateTime fechaCreacion;
    private final LocalDateTime fechaExpiracion;
    private boolean usado;
    private boolean revocado;

    public TokenDesbloqueo(String token, String userId, LocalDateTime fechaCreacion,
                           LocalDateTime fechaExpiracion, boolean usado, boolean revocado) {
        this.token = Objects.requireNonNull(token, "El token no puede ser nulo");
        this.userId = Objects.requireNonNull(userId, "El userId no puede ser nulo");
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion, "La fecha de creación no puede ser nula");
        this.fechaExpiracion = Objects.requireNonNull(fechaExpiracion, "La fecha de expiración no puede ser nula");
        this.usado = usado;
        this.revocado = revocado;
    }

    /**
     * Factory method para generar un nuevo token con vigencia de 1 hora.
     */
    public static TokenDesbloqueo generarNuevo(String userId) {
        LocalDateTime ahora = LocalDateTime.now();
        return new TokenDesbloqueo(
                UUID.randomUUID().toString(),
                userId,
                ahora,
                ahora.plusHours(HORAS_VIGENCIA),
                false,
                false
        );
    }

    /**
     * Un token es válido si no ha expirado, no ha sido usado y no ha sido revocado.
     */
    public boolean esValido(LocalDateTime fechaActual) {
        return !fechaActual.isAfter(fechaExpiracion) && !usado && !revocado;
    }

    /**
     * Verifica si el token ha expirado (sin considerar uso o revocación).
     */
    public boolean estaExpirado(LocalDateTime fechaActual) {
        return fechaActual.isAfter(fechaExpiracion);
    }

    public void marcarUsado() {
        this.usado = true;
    }

    public void revocar() {
        this.revocado = true;
    }

    // Getters
    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public boolean isUsado() { return usado; }
    public boolean isRevocado() { return revocado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenDesbloqueo that = (TokenDesbloqueo) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }
}
