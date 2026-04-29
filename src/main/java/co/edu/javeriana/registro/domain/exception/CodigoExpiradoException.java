package co.edu.javeriana.registro.domain.exception;

public class CodigoExpiradoException extends DomainException {
    public CodigoExpiradoException() {
        super("El código de validación ha expirado");
    }
}
