package co.edu.javeriana.registro.domain.exception;

public class CuentaNoVerificadaException extends DomainException {
    public CuentaNoVerificadaException() {
        super("Debe verificar su email antes de iniciar sesión");
    }
}
