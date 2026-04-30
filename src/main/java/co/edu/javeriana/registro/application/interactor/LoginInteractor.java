package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.TokenDesbloqueoGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.exception.CuentaBloqueadaException;
import co.edu.javeriana.registro.domain.exception.CuentaNoVerificadaException;
import co.edu.javeriana.registro.domain.exception.CuentaSuspendidaException;
import co.edu.javeriana.registro.domain.exception.CredencialesInvalidasException;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.ResultadoAutenticacion;
import co.edu.javeriana.registro.domain.model.TokenDesbloqueo;
import co.edu.javeriana.registro.domain.model.Usuario;

import java.time.LocalDateTime;

/**
 * Caso de uso HU-AUTH-001: Inicio de sesión como buyer.
 * Integra HU-AUTH-002: Bloqueo por intentos fallidos.
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Autenticar con email + contraseña</li>
 *   <li>Validar estado de la cuenta (ACTIVO, NO_VERIFICADO, BLOQUEADO, SUSPENDIDO)</li>
 *   <li>Gestionar intentos fallidos y bloqueo automático</li>
 *   <li>Reiniciar contador en login exitoso</li>
 * </ul>
 */
public class LoginInteractor {

    private static final int MAX_INTENTOS = 3;       // RN-AUTH-006 modificado
    private static final long MINUTOS_BLOQUEO = 30;   // RN-AUTH-007

    private final UsuarioGateway usuarioGateway;
    private final EmailGateway emailGateway;
    private final TokenDesbloqueoGateway tokenGateway;

    public LoginInteractor(UsuarioGateway usuarioGateway, EmailGateway emailGateway,
                           TokenDesbloqueoGateway tokenGateway) {
        this.usuarioGateway = usuarioGateway;
        this.emailGateway = emailGateway;
        this.tokenGateway = tokenGateway;
    }

    public ResultadoAutenticacion ejecutar(String email, String password) {
        LocalDateTime ahora = LocalDateTime.now();

        // Buscar usuario — mensaje genérico si no existe (RN-AUTH-001)
        Usuario usuario = usuarioGateway.buscarPorEmail(email)
                .orElseThrow(CredencialesInvalidasException::new);

        // Verificar estado de la cuenta
        verificarEstadoCuenta(usuario, ahora);

        // Verificar credenciales
        if (!usuario.verificarPassword(password)) {
            return procesarIntentoFallido(usuario, ahora);
        }

        // Login exitoso — reiniciar contador (AC-005 de HU-AUTH-002)
        usuario.reiniciarIntentosFallidos();
        usuarioGateway.guardar(usuario);

        return ResultadoAutenticacion.exitoso(usuario);
    }

    private void verificarEstadoCuenta(Usuario usuario, LocalDateTime ahora) {
        switch (usuario.getEstado()) {
            case NO_VERIFICADO:
            case CADUCADO:
                throw new CuentaNoVerificadaException();
            case SUSPENDIDO:
                throw new CuentaSuspendidaException();
            case BLOQUEADO:
                if (usuario.estaBloqueado(ahora, MINUTOS_BLOQUEO)) {
                    long minutosRestantes = usuario.minutosRestantesBloqueo(ahora, MINUTOS_BLOQUEO);
                    throw new CuentaBloqueadaException(minutosRestantes);
                }
                // Bloqueo expirado → desbloquear automáticamente (AC-003 de HU-AUTH-002)
                usuario.desbloquear();
                usuarioGateway.guardar(usuario);
                break;
            case ACTIVO:
                // OK
                break;
        }
    }

    private ResultadoAutenticacion procesarIntentoFallido(Usuario usuario, LocalDateTime ahora) {
        boolean bloqueado = usuario.registrarIntentoFallido(MAX_INTENTOS, ahora);
        usuarioGateway.guardar(usuario);

        if (bloqueado) {
            // Enviar notificación de bloqueo (AC-002 de HU-AUTH-002)
            emailGateway.enviarNotificacionBloqueo(usuario.getEmail(), usuario.getNombre(), ahora);

            // Generar y enviar token de desbloqueo (AC-001 de HU-AUTH-005)
            TokenDesbloqueo token = TokenDesbloqueo.generarNuevo(usuario.getId());
            tokenGateway.guardar(token);
            emailGateway.enviarEnlaceDesbloqueo(
                    usuario.getEmail(), usuario.getNombre(),
                    token.getToken(), token.getFechaExpiracion());

            throw new CuentaBloqueadaException(MINUTOS_BLOQUEO);
        }

        // Mensaje genérico — no revela si el email o la contraseña fallaron (RN-AUTH-001)
        throw new CredencialesInvalidasException();
    }
}
