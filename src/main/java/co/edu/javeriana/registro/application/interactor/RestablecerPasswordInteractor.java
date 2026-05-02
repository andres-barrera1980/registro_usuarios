package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.model.TokenRecuperacion;
import co.edu.javeriana.registro.domain.model.Usuario;

import java.time.LocalDateTime;

public class RestablecerPasswordInteractor {

    private final UsuarioGateway usuarioGateway;
    private final EmailGateway emailGateway;

    public RestablecerPasswordInteractor(UsuarioGateway usuarioGateway, EmailGateway emailGateway) {
        this.usuarioGateway = usuarioGateway;
        this.emailGateway = emailGateway;
    }

    public void ejecutar(String email, String tokenRecibido, String nuevoPassword) {
        Usuario usuario = usuarioGateway.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado o token inválido")); // Mensaje genérico para no filtrar info

        // Dummy token object para comparar en el dominio
        TokenRecuperacion tokenValidar = new TokenRecuperacion(tokenRecibido, LocalDateTime.now().plusDays(1));

        // Valida expiración y estado en el dominio, actualiza password
        usuario.restablecerPassword(nuevoPassword, tokenValidar, LocalDateTime.now());

        usuarioGateway.guardar(usuario);

        // AC-005: Cerrar sesiones
        // En una app real aquí se invalidarían tokens JWT o sesiones HTTP en algún SessionGateway.
        // Simulamos el cierre de sesión:
        System.out.println("Sesiones activas cerradas para el usuario: " + email);

        emailGateway.enviarConfirmacionCambioPassword(usuario.getEmail());
    }
}
