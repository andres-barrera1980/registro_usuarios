package co.edu.javeriana.registro.domain.exception;

public class CuentaSuspendidaException extends DomainException {
    public CuentaSuspendidaException() {
        super("La cuenta se encuentra suspendida. Contacte a soporte");
    }
}
