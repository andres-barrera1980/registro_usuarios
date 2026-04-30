package co.edu.javeriana.registro.domain.model;

import co.edu.javeriana.registro.domain.exception.CodigoExpiradoException;
import co.edu.javeriana.registro.domain.exception.CodigoInvalidoException;
import co.edu.javeriana.registro.domain.exception.CuentaYaActivaException;

import java.time.LocalDateTime;
import java.util.Objects;

public class Usuario {
    private final String id;
    private final String nombre;
    private final String email;
    private final String passwordHash;
    private EstadoUsuario estado;
    private CodigoValidacion codigoValidacionActivo;
    private final LocalDateTime fechaRegistro;

    public Usuario(String id, String nombre, String email, String rawPassword, PasswordHasher hasher) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        validarId(this.id);
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        validarNombre(this.nombre);
        this.email = Objects.requireNonNull(email, "El email no puede ser nulo");
        validarEmail(this.email);
        Objects.requireNonNull(rawPassword, "La contraseña no puede ser nula");
        Objects.requireNonNull(hasher, "El hasher no puede ser nulo");
        validarPassword(rawPassword);
        this.passwordHash = hasher.hash(rawPassword);
        this.estado = EstadoUsuario.NO_VERIFICADO;
        this.fechaRegistro = LocalDateTime.now();
    }

    /**
     * Constructor de rehidratación para reconstruir un Usuario desde persistencia.
     * No aplica validación de contraseña porque el hash ya fue generado previamente.
     */
    public Usuario(String id, String nombre, String email, String passwordHash,
                   EstadoUsuario estado, LocalDateTime fechaRegistro) {
        this.id = Objects.requireNonNull(id);
        validarId(this.id);
        this.nombre = Objects.requireNonNull(nombre);
        validarNombre(this.nombre);
        this.email = Objects.requireNonNull(email);
        validarEmail(this.email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.estado = Objects.requireNonNull(estado);
        this.fechaRegistro = Objects.requireNonNull(fechaRegistro);
    }

    private void validarEmail(String email) {
        String regexPattern = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!email.matches(regexPattern)) {
            throw new IllegalArgumentException("Por favor ingrese una dirección de correo electrónico válida");
        }
    }

    private void validarId(String id) {
        if (!id.matches("^[0-9]{10}$")) {
            throw new IllegalArgumentException("El ID debe tener exactamente 10 caracteres numéricos");
        }
    }

    private void validarNombre(String nombre) {
        if (nombre.trim().length() < 2) {
            throw new IllegalArgumentException("El nombre debe tener al menos 2 caracteres");
        }
    }

    private void validarPassword(String rawPassword) {
        if (rawPassword.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una letra mayúscula");
        }
        if (!rawPassword.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos un número");
        }
        if (!rawPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos un carácter especial");
        }
    }

    /**
     * Verifica si una contraseña en texto plano coincide con el hash almacenado.
     */
    public boolean verificarPassword(String rawPassword, PasswordHasher hasher) {
        return hasher.matches(rawPassword, this.passwordHash);
    }

    public void asignarNuevoCodigo(CodigoValidacion nuevoCodigo) {
        if (this.estado == EstadoUsuario.ACTIVO) {
            throw new CuentaYaActivaException();
        }
        this.codigoValidacionActivo = nuevoCodigo;
    }

    public void activarCuenta(String codigoRecibido, LocalDateTime fechaActual) {
        if (this.estado == EstadoUsuario.ACTIVO) {
            return; // Ya está activa, no hacer nada (según AC-004 de HU2)
        }

        if (codigoValidacionActivo == null || !codigoValidacionActivo.getCodigo().equals(codigoRecibido)) {
            throw new CodigoInvalidoException();
        }

        if (codigoValidacionActivo.estaExpirado(fechaActual)) {
            this.estado = EstadoUsuario.CADUCADO;
            throw new CodigoExpiradoException();
        }

        this.estado = EstadoUsuario.ACTIVO;
        this.codigoValidacionActivo = null; // Limpiar código una vez usado
    }

    public boolean esElegibleParaLimpieza(LocalDateTime fechaActual) {
        if (this.estado == EstadoUsuario.ACTIVO) {
            return false;
        }
        
        // Si no tiene código y no está activo, o si el código expiró hace más de 7 días
        if (codigoValidacionActivo == null) {
            return true; // O definir lógica basada en fechaRegistro
        }

        return codigoValidacionActivo.getFechaExpiracion().plusDays(7).isBefore(fechaActual);
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public EstadoUsuario getEstado() { return estado; }
    public CodigoValidacion getCodigoValidacionActivo() { return codigoValidacionActivo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
