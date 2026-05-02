package co.edu.javeriana.registro.domain.exception;

public class TokenInvalidoException extends DomainException {
    public TokenInvalidoException() {
        super("El token proporcionado es inválido o ha expirado.");
    }
}
