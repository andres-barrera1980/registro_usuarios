package co.edu.javeriana.registro.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class CodigoValidacion {
    private final String codigo;
    private final LocalDateTime fechaExpiracion;

    public CodigoValidacion(String codigo, LocalDateTime fechaExpiracion) {
        this.codigo = Objects.requireNonNull(codigo, "El código no puede ser nulo");
        this.fechaExpiracion = Objects.requireNonNull(fechaExpiracion, "La fecha de expiración no puede ser nula");
    }

    public static CodigoValidacion generarNuevo() {
        return new CodigoValidacion(
            UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            LocalDateTime.now().plusHours(24)
        );
    }

    public boolean estaExpirado(LocalDateTime fechaActual) {
        return fechaActual.isAfter(fechaExpiracion);
    }

    public String getCodigo() {
        return codigo;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodigoValidacion that = (CodigoValidacion) o;
        return Objects.equals(codigo, that.codigo) && Objects.equals(fechaExpiracion, that.fechaExpiracion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo, fechaExpiracion);
    }
}
