package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

public class LimpiarCuentasInactivasInteractor {

    private final UsuarioGateway usuarioGateway;

    public LimpiarCuentasInactivasInteractor(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    public void ejecutar() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Usuario> inactivos = usuarioGateway.buscarCuentasInactivasExpiradas(ahora);

        for (Usuario usuario : inactivos) {
            if (usuario.esElegibleParaLimpieza(ahora)) {
                usuarioGateway.eliminar(usuario);
            }
        }
    }
}
