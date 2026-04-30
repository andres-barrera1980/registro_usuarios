package co.edu.javeriana.registro.domain.exception;

public class CuentaBloqueadaException extends DomainException {

    private final long minutosRestantes;

    public CuentaBloqueadaException(long minutosRestantes) {
        super("La cuenta está bloqueada. Intente nuevamente en " + minutosRestantes + " minutos");
        this.minutosRestantes = minutosRestantes;
    }

    public long getMinutosRestantes() {
        return minutosRestantes;
    }
}
