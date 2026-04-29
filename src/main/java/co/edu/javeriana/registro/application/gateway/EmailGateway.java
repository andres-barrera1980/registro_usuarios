package co.edu.javeriana.registro.application.gateway;

public interface EmailGateway {
    void enviarCodigoValidacion(String emailDestino, String codigo);
}
