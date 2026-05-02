package co.edu.javeriana.registro.domain.model;

import co.edu.javeriana.registro.domain.exception.CodigoExpiradoException;
import co.edu.javeriana.registro.domain.exception.CodigoInvalidoException;
import co.edu.javeriana.registro.domain.exception.CuentaYaActivaException;
import co.edu.javeriana.registro.domain.exception.UsuarioBloqueadoException;
import co.edu.javeriana.registro.domain.exception.TokenInvalidoException;

import java.time.LocalDateTime;
import java.util.Objects;

public class Usuario {
    private final String id;
    private final String nombre;
    private final String email;
    private String password;
    private EstadoUsuario estado;
    private CodigoValidacion codigoValidacionActivo;
    private TokenRecuperacion tokenRecuperacion;
    private final LocalDateTime fechaRegistro;

    public Usuario(String id, String nombre, String email) {
        this.id = Objects.requireNonNull(id, "El ID no puede ser nulo");
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.email = Objects.requireNonNull(email, "El email no puede ser nulo");
        this.estado = EstadoUsuario.NO_VERIFICADO;
        this.fechaRegistro = LocalDateTime.now();
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

    public TokenRecuperacion generarTokenRecuperacion(String tokenString, LocalDateTime expiracion) {
        if (this.estado == EstadoUsuario.BLOQUEADO) {
            throw new UsuarioBloqueadoException();
        }
        
        this.tokenRecuperacion = new TokenRecuperacion(tokenString, expiracion);
        return this.tokenRecuperacion;
    }

    public void restablecerPassword(String nuevoPassword, TokenRecuperacion token, LocalDateTime fechaActual) {
        if (this.tokenRecuperacion == null || !this.tokenRecuperacion.getToken().equals(token.getToken())) {
            throw new TokenInvalidoException();
        }
        
        if (!this.tokenRecuperacion.esValido(fechaActual)) {
            throw new TokenInvalidoException();
        }

        this.password = nuevoPassword;
        this.tokenRecuperacion.marcarComoUsado();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public TokenRecuperacion getTokenRecuperacion() {
        return tokenRecuperacion;
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public EstadoUsuario getEstado() { return estado; }
    public CodigoValidacion getCodigoValidacionActivo() { return codigoValidacionActivo; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
