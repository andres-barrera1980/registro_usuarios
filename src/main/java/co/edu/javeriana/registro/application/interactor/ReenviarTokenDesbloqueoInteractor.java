package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.TokenDesbloqueoGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.exception.CuentaSuspendidaException;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.TokenDesbloqueo;
import co.edu.javeriana.registro.domain.model.Usuario;

/**
 * Caso de uso HU-AUTH-005 AC-005: Reenvío de token de desbloqueo.
 * 
 * <p>Invalida tokens anteriores y genera uno nuevo. Solo aplica a cuentas BLOQUEADAS.
 */
public class ReenviarTokenDesbloqueoInteractor {

    private final UsuarioGateway usuarioGateway;
    private final TokenDesbloqueoGateway tokenGateway;
    private final EmailGateway emailGateway;

    public ReenviarTokenDesbloqueoInteractor(UsuarioGateway usuarioGateway,
                                              TokenDesbloqueoGateway tokenGateway,
                                              EmailGateway emailGateway) {
        this.usuarioGateway = usuarioGateway;
        this.tokenGateway = tokenGateway;
        this.emailGateway = emailGateway;
    }

    public void ejecutar(String email) {
        Usuario usuario = usuarioGateway.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

        // Solo aplica a cuentas bloqueadas (RN-AUTH-019)
        if (usuario.getEstado() == EstadoUsuario.SUSPENDIDO) {
            throw new CuentaSuspendidaException();
        }

        if (usuario.getEstado() != EstadoUsuario.BLOQUEADO) {
            throw new IllegalStateException("La cuenta no está bloqueada. No es posible enviar enlace de desbloqueo");
        }

        // Revocar tokens previos (RN-AUTH-018)
        tokenGateway.revocarTokensDeUsuario(usuario.getId());

        // Generar nuevo token
        TokenDesbloqueo nuevoToken = TokenDesbloqueo.generarNuevo(usuario.getId());
        tokenGateway.guardar(nuevoToken);

        // Enviar email con nuevo enlace
        emailGateway.enviarEnlaceDesbloqueo(
                usuario.getEmail(), usuario.getNombre(),
                nuevoToken.getToken(), nuevoToken.getFechaExpiracion());
    }
}
