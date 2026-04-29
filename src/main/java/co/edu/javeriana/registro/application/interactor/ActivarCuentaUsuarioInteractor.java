package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.model.Usuario;

import java.time.LocalDateTime;

public class ActivarCuentaUsuarioInteractor {

    private final UsuarioGateway usuarioGateway;

    public ActivarCuentaUsuarioInteractor(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    public void ejecutar(String email, String codigoValidacion) {
        Usuario usuario = usuarioGateway.buscarPorEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

        usuario.activarCuenta(codigoValidacion, LocalDateTime.now());
        
        usuarioGateway.guardar(usuario);
    }
}
