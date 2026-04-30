package co.edu.javeriana.registro.domain.model;

import co.edu.javeriana.registro.domain.exception.CodigoExpiradoException;
import co.edu.javeriana.registro.domain.exception.CodigoInvalidoException;
import co.edu.javeriana.registro.domain.exception.CuentaYaActivaException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Objects;

public class Usuario {
    private final String id;
    private final String nombre;
    private final String email;
    private EstadoUsuario estado;
    private CodigoValidacion codigoValidacionActivo;
    private final LocalDateTime fechaRegistro;
    private String passwordHash;
    private int intentosFallidos;
    private LocalDateTime fechaBloqueo;

    public Usuario(String id, String nombre, String email) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.email = Objects.requireNonNull(email, "El email no puede ser nulo");
        this.estado = EstadoUsuario.NO_VERIFICADO;
        this.fechaRegistro = LocalDateTime.now();
        this.intentosFallidos = 0;
    }

    public Usuario(String id, String nombre, String email, String passwordPlano) {
        this(id, nombre, email);
        this.passwordHash = hashPassword(passwordPlano);
    }

    // --- Validación de cuenta (existente) ---

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

    // --- Autenticación (HU-AUTH-001) ---

    /**
     * Verifica si la contraseña proporcionada coincide con el hash almacenado.
     */
    public boolean verificarPassword(String passwordPlano) {
        if (this.passwordHash == null || passwordPlano == null) {
            return false;
        }
        return this.passwordHash.equals(hashPassword(passwordPlano));
    }

    // --- Bloqueo por intentos fallidos (HU-AUTH-002) ---

    /**
     * Registra un intento fallido de login. Si se supera el máximo permitido,
     * la cuenta pasa a estado BLOQUEADO.
     * 
     * @param maxIntentos número máximo de intentos antes de bloquear (RN-AUTH-006: 5)
     * @param ahora momento actual para registrar la fecha de bloqueo
     * @return true si la cuenta fue bloqueada con este intento
     */
    public boolean registrarIntentoFallido(int maxIntentos, LocalDateTime ahora) {
        this.intentosFallidos++;
        if (this.intentosFallidos >= maxIntentos) {
            this.estado = EstadoUsuario.BLOQUEADO;
            this.fechaBloqueo = ahora;
            return true;
        }
        return false;
    }

    /**
     * Reinicia el contador de intentos fallidos a cero (AC-005 de HU-AUTH-002).
     */
    public void reiniciarIntentosFallidos() {
        this.intentosFallidos = 0;
    }

    /**
     * Verifica si la cuenta está actualmente bajo bloqueo temporal.
     * El bloqueo expira después de los minutos configurados (RN-AUTH-007: 30 min).
     * 
     * @param ahora momento actual
     * @param minutosBloqueo duración del bloqueo en minutos
     * @return true si la cuenta sigue bloqueada
     */
    public boolean estaBloqueado(LocalDateTime ahora, long minutosBloqueo) {
        if (this.estado != EstadoUsuario.BLOQUEADO || this.fechaBloqueo == null) {
            return false;
        }
        return ahora.isBefore(this.fechaBloqueo.plusMinutes(minutosBloqueo));
    }

    /**
     * Calcula los minutos restantes de bloqueo.
     */
    public long minutosRestantesBloqueo(LocalDateTime ahora, long minutosBloqueo) {
        if (this.fechaBloqueo == null) {
            return 0;
        }
        LocalDateTime finBloqueo = this.fechaBloqueo.plusMinutes(minutosBloqueo);
        if (ahora.isAfter(finBloqueo)) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(ahora, finBloqueo) + 1;
    }

    /**
     * Desbloquea la cuenta, cambiando el estado a ACTIVO y reiniciando contadores.
     * Solo aplica a cuentas en estado BLOQUEADO (no SUSPENDIDO).
     */
    public void desbloquear() {
        this.estado = EstadoUsuario.ACTIVO;
        this.intentosFallidos = 0;
        this.fechaBloqueo = null;
    }

    // --- Hashing de contraseña (SHA-256) ---

    static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 no disponible", e);
        }
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public EstadoUsuario getEstado() { return estado; }
    public CodigoValidacion getCodigoValidacionActivo() { return codigoValidacionActivo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public String getPasswordHash() { return passwordHash; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public LocalDateTime getFechaBloqueo() { return fechaBloqueo; }
}
