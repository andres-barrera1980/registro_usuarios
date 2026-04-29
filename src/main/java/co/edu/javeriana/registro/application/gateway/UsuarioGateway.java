package co.edu.javeriana.registro.application.gateway;

import co.edu.javeriana.registro.domain.model.Usuario;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioGateway {
    void guardar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String email);
    List<Usuario> buscarCuentasInactivasExpiradas(LocalDateTime fechaCorte);
    void eliminar(Usuario usuario);
}
