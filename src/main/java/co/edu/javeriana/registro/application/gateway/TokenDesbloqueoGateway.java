package co.edu.javeriana.registro.application.gateway;

import co.edu.javeriana.registro.domain.model.TokenDesbloqueo;

import java.util.Optional;

public interface TokenDesbloqueoGateway {
    void guardar(TokenDesbloqueo token);
    Optional<TokenDesbloqueo> buscarPorToken(String token);
    void revocarTokensDeUsuario(String userId);
    Optional<TokenDesbloqueo> buscarUltimoTokenDeUsuario(String userId);
}
