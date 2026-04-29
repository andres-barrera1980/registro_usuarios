package co.edu.javeriana.registro.domain.exception;

public class CodigoInvalidoException extends DomainException {
    public CodigoInvalidoException() {
        super("El código de validación es incorrecto");
    }
}
