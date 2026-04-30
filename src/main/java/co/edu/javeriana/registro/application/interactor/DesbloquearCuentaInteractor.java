package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.TokenDesbloqueoGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.exception.CuentaSuspendidaException;
import co.edu.javeriana.registro.domain.exception.TokenExpiradoException;
import co.edu.javeriana.registro.domain.exception.TokenInvalidoException;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.TokenDesbloqueo;
import co.edu.javeriana.registro.domain.model.Usuario;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Caso de uso HU-AUTH-005: Desbloqueo de cuenta mediante token enviado por email.
 * 
 * <p>Valida el token, verifica que la cuenta esté en estado BLOQUEADO (no SUSPENDIDO),
 * y reactiva la cuenta reiniciando el contador de intentos fallidos.
 */
public class DesbloquearCuentaInteractor {

    private final TokenDesbloqueoGateway tokenGateway;
    private final UsuarioGateway usuarioGateway;

    public DesbloquearCuentaInteractor(TokenDesbloqueoGateway tokenGateway, UsuarioGateway usuarioGateway) {
        this.tokenGateway = tokenGateway;
        this.usuarioGateway = usuarioGateway;
    }

    public Optional<String> buscarUltimoToken(String email) {
        return usuarioGateway.buscarPorEmail(email)
                .flatMap(u -> tokenGateway.buscarUltimoTokenDeUsuario(u.getId()))
                .map(TokenDesbloqueo::getToken);
    }

    public void ejecutar(String email, String tokenStr) {
        LocalDateTime ahora = LocalDateTime.now();

        TokenDesbloqueo token = tokenGateway.buscarPorToken(tokenStr)
                .orElseThrow(() -> new TokenInvalidoException("El enlace de desbloqueo no es válido"));

        // Validar que el token pertenezca al email proporcionado
        Usuario usuario = usuarioGateway.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

        if (!token.getUserId().equals(usuario.getId())) {
            throw new TokenInvalidoException("El token proporcionado no pertenece a esta cuenta");
        }
        if (token.isUsado()) {
            throw new TokenInvalidoException("El enlace de desbloqueo ya fue utilizado");
        }

        // Verificar si fue revocado
        if (token.isRevocado()) {
            throw new TokenInvalidoException("El enlace de desbloqueo ha sido invalidado");
        }

        // Verificar expiración (AC-003)
        if (token.estaExpirado(ahora)) {
            throw new TokenExpiradoException();
        }

        // Solo se puede desbloquear cuentas en estado BLOQUEADO (RN-AUTH-019, AC-006)
        if (usuario.getEstado() == EstadoUsuario.SUSPENDIDO) {
            throw new CuentaSuspendidaException();
        }

        // Desbloquear la cuenta (AC-002)
        usuario.desbloquear();
        token.marcarUsado();

        usuarioGateway.guardar(usuario);
        tokenGateway.guardar(token);
    }
}
