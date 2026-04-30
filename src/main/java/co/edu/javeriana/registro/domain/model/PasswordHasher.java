package co.edu.javeriana.registro.domain.model;

/**
 * Puerto de dominio para el hashing de contraseñas.
 * La implementación concreta (BCrypt, Argon2, etc.) vive en la capa de infraestructura.
 */
public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
