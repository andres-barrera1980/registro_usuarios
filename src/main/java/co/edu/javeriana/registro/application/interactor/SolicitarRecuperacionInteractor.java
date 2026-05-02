package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.model.TokenRecuperacion;
import co.edu.javeriana.registro.domain.model.Usuario;
import co.edu.javeriana.registro.domain.exception.UsuarioBloqueadoException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class SolicitarRecuperacionInteractor {

    private final UsuarioGateway usuarioGateway;
    private final EmailGateway emailGateway;

    public SolicitarRecuperacionInteractor(UsuarioGateway usuarioGateway, EmailGateway emailGateway) {
        this.usuarioGateway = usuarioGateway;
        this.emailGateway = emailGateway;
    }

    public void ejecutar(String email) {
        // Por seguridad AC-003, AC-002: El mensaje final siempre es genérico,
        // no lanzamos excepciones si el usuario no existe para no filtrar emails válidos.
        try {
            Optional<Usuario> usuarioOpt = usuarioGateway.buscarPorEmail(email);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                String tokenString = UUID.randomUUID().toString();
                LocalDateTime expiracion = LocalDateTime.now().plusMinutes(15); // Expiración en 15 mins
                
                TokenRecuperacion token = usuario.generarTokenRecuperacion(tokenString, expiracion);
                
                usuarioGateway.guardar(usuario);
                emailGateway.enviarEnlaceRecuperacion(usuario.getEmail(), token.getToken());
            }
        } catch (UsuarioBloqueadoException e) {
            // AC-008: Si está bloqueado, no procesamos la solicitud pero el usuario
            // externamente igual recibe el mensaje general de seguridad.
            // Se podría loggear internamente.
            System.err.println("Intento de recuperación de cuenta bloqueada: " + email);
        } catch (Exception e) {
            System.err.println("Error en recuperación: " + e.getMessage());
        }
    }
}
