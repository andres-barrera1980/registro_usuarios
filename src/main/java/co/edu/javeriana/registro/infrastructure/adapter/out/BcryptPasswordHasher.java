package co.edu.javeriana.registro.infrastructure.adapter.out;

import at.favre.lib.crypto.bcrypt.BCrypt;
import co.edu.javeriana.registro.domain.model.PasswordHasher;

/**
 * Implementación de PasswordHasher usando BCrypt (at.favre.lib:bcrypt).
 * Cost factor de 12 proporciona un buen balance entre seguridad y rendimiento.
 */
public class BcryptPasswordHasher implements PasswordHasher {

    private static final int COST = 12;

    @Override
    public String hash(String rawPassword) {
        return BCrypt.withDefaults().hashToString(COST, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}
