package co.edu.javeriana.registro.application.gateway;

import java.time.LocalDateTime;

public interface EmailGateway {
    void enviarCodigoValidacion(String emailDestino, String codigo);
    void enviarNotificacionBloqueo(String emailDestino, String nombreUsuario, LocalDateTime horaBloqueo);
    void enviarEnlaceDesbloqueo(String emailDestino, String nombreUsuario, String token, LocalDateTime expiracion);
}
