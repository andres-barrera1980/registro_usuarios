package co.edu.javeriana.registro.domain.exception;

public class TokenExpiradoException extends DomainException {
    public TokenExpiradoException() {
        super("El enlace de desbloqueo ha expirado. Solicite uno nuevo");
    }
}
