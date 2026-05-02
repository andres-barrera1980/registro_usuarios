package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.model.CodigoValidacion;
import co.edu.javeriana.registro.domain.model.Usuario;

public class EmitirCodigoValidacionInteractor {

    private final UsuarioGateway usuarioGateway;
    private final EmailGateway emailGateway;

    public EmitirCodigoValidacionInteractor(UsuarioGateway usuarioGateway, EmailGateway emailGateway) {
        this.usuarioGateway = usuarioGateway;
        this.emailGateway = emailGateway;
    }

    public void ejecutar(String email) {
        Usuario usuario = usuarioGateway.buscarPorEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));

        CodigoValidacion nuevoCodigo = CodigoValidacion.generarNuevo();
        usuario.asignarNuevoCodigo(nuevoCodigo);

        usuarioGateway.guardar(usuario);
        emailGateway.enviarCodigoValidacion(usuario.getEmail(), nuevoCodigo.getCodigo());
    }
}
