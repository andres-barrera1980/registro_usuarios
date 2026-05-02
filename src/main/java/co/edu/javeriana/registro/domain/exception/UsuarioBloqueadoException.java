package co.edu.javeriana.registro.domain.exception;

public class UsuarioBloqueadoException extends DomainException {
    public UsuarioBloqueadoException() {
        super("El usuario se encuentra bloqueado.");
    }
}
