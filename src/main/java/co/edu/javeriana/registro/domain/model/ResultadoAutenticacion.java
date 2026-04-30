package co.edu.javeriana.registro.domain.model;

import java.util.Objects;

/**
 * Value Object que encapsula el resultado de un intento de autenticación.
 * Evita exponer detalles internos del proceso de login.
 */
public final class ResultadoAutenticacion {

    private final boolean exitoso;
    private final String mensaje;
    private final Usuario usuario;

    private ResultadoAutenticacion(boolean exitoso, String mensaje, Usuario usuario) {
        this.exitoso = exitoso;
        this.mensaje = Objects.requireNonNull(mensaje, "El mensaje no puede ser nulo");
        this.usuario = usuario;
    }

    public static ResultadoAutenticacion exitoso(Usuario usuario) {
        return new ResultadoAutenticacion(true, "Inicio de sesión exitoso", 
                Objects.requireNonNull(usuario, "El usuario no puede ser nulo en un login exitoso"));
    }

    public static ResultadoAutenticacion fallido(String mensaje) {
        return new ResultadoAutenticacion(false, mensaje, null);
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
