package co.edu.javeriana.registro.domain.exception;

public class CuentaYaActivaException extends DomainException {
    public CuentaYaActivaException() {
        super("La cuenta ya se encuentra activa");
    }
}
