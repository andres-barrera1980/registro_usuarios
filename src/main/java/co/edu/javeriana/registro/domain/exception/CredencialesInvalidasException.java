package co.edu.javeriana.registro.domain.exception;

public class CredencialesInvalidasException extends DomainException {
    public CredencialesInvalidasException() {
        super("Email o contraseña incorrectos");
    }
}
