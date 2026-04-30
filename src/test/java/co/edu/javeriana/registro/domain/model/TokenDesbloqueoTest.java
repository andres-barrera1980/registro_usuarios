package co.edu.javeriana.registro.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TokenDesbloqueoTest {

    private final LocalDateTime ahora = LocalDateTime.now();

    @Test
    void deberiaGenerarTokenConVigencia1Hora() {
        TokenDesbloqueo token = TokenDesbloqueo.generarNuevo("user-1");

        assertNotNull(token.getToken());
        assertEquals("user-1", token.getUserId());
        assertFalse(token.isUsado());
        assertFalse(token.isRevocado());
        assertTrue(token.getFechaExpiracion().isAfter(ahora.plusMinutes(59)));
        assertTrue(token.getFechaExpiracion().isBefore(ahora.plusMinutes(61)));
    }

    @Test
    void deberiaSerValidoSiNoExpiradoNiUsadoNiRevocado() {
        TokenDesbloqueo token = new TokenDesbloqueo(
                "token-123", "user-1", ahora, ahora.plusHours(1), false, false);

        assertTrue(token.esValido(ahora));
        assertTrue(token.esValido(ahora.plusMinutes(30)));
    }

    @Test
    void deberiaSerInvalidoSiExpirado() {
        TokenDesbloqueo token = new TokenDesbloqueo(
                "token-123", "user-1", ahora.minusHours(2), ahora.minusHours(1), false, false);

        assertFalse(token.esValido(ahora));
        assertTrue(token.estaExpirado(ahora));
    }

    @Test
    void deberiaSerInvalidoSiUsado() {
        TokenDesbloqueo token = new TokenDesbloqueo(
                "token-123", "user-1", ahora, ahora.plusHours(1), true, false);

        assertFalse(token.esValido(ahora));
    }

    @Test
    void deberiaSerInvalidoSiRevocado() {
        TokenDesbloqueo token = new TokenDesbloqueo(
                "token-123", "user-1", ahora, ahora.plusHours(1), false, true);

        assertFalse(token.esValido(ahora));
    }

    @Test
    void deberiaMarcarseComoUsado() {
        TokenDesbloqueo token = TokenDesbloqueo.generarNuevo("user-1");
        assertFalse(token.isUsado());

        token.marcarUsado();

        assertTrue(token.isUsado());
        assertFalse(token.esValido(ahora));
    }

    @Test
    void deberiaRevocarse() {
        TokenDesbloqueo token = TokenDesbloqueo.generarNuevo("user-1");
        assertFalse(token.isRevocado());

        token.revocar();

        assertTrue(token.isRevocado());
        assertFalse(token.esValido(ahora));
    }
}
