package co.edu.javeriana.registro.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioLoginTest {

    private Usuario usuario;
    private LocalDateTime ahora;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("1", "Andres", "andres@mail.com", "MiPassword123");
        ahora = LocalDateTime.now();
    }

    // --- Verificación de contraseña ---

    @Test
    void deberiaVerificarPasswordCorrecto() {
        assertTrue(usuario.verificarPassword("MiPassword123"));
    }

    @Test
    void deberiaRechazarPasswordIncorrecto() {
        assertFalse(usuario.verificarPassword("PasswordIncorrecto"));
    }

    @Test
    void deberiaRechazarPasswordNulo() {
        assertFalse(usuario.verificarPassword(null));
    }

    @Test
    void deberiaCrearUsuarioSinPasswordYRechazarVerificacion() {
        Usuario sinPassword = new Usuario("2", "User", "user@mail.com");
        assertFalse(sinPassword.verificarPassword("cualquiera"));
    }

    // --- Intentos fallidos y bloqueo ---

    @Test
    void deberiaIncrementarIntentosFallidos() {
        usuario.registrarIntentoFallido(3, ahora);
        assertEquals(1, usuario.getIntentosFallidos());
    }

    @Test
    void noDeberiaBloquearseCon2IntentosFallidos() {
        for (int i = 0; i < 2; i++) {
            boolean bloqueado = usuario.registrarIntentoFallido(3, ahora);
            assertFalse(bloqueado);
        }
        assertEquals(2, usuario.getIntentosFallidos());
        assertNotEquals(EstadoUsuario.BLOQUEADO, usuario.getEstado());
    }

    @Test
    void deberiaBloquearseTras3IntentosFallidos() {
        for (int i = 0; i < 2; i++) {
            usuario.registrarIntentoFallido(3, ahora);
        }
        boolean bloqueado = usuario.registrarIntentoFallido(3, ahora);

        assertTrue(bloqueado);
        assertEquals(EstadoUsuario.BLOQUEADO, usuario.getEstado());
        assertEquals(3, usuario.getIntentosFallidos());
        assertNotNull(usuario.getFechaBloqueo());
    }

    @Test
    void deberiaEstarBloqueadoDuranteVentanaDe30Minutos() {
        // Activar primero
        CodigoValidacion codigo = new CodigoValidacion("CODE", ahora.plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        usuario.activarCuenta("CODE", ahora);

        // Bloquear
        for (int i = 0; i < 3; i++) {
            usuario.registrarIntentoFallido(3, ahora);
        }

        // Verificar durante la ventana
        assertTrue(usuario.estaBloqueado(ahora.plusMinutes(10), 30));
        assertTrue(usuario.estaBloqueado(ahora.plusMinutes(29), 30));
    }

    @Test
    void deberiaDesbloquearseAutoTrasPasarVentana() {
        // Activar primero
        CodigoValidacion codigo = new CodigoValidacion("CODE", ahora.plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        usuario.activarCuenta("CODE", ahora);

        // Bloquear
        for (int i = 0; i < 3; i++) {
            usuario.registrarIntentoFallido(3, ahora);
        }

        // Verificar que ya no está bloqueado después de la ventana
        assertFalse(usuario.estaBloqueado(ahora.plusMinutes(31), 30));
    }

    @Test
    void deberiaCalcularMinutosRestantesDeBloqueo() {
        for (int i = 0; i < 3; i++) {
            usuario.registrarIntentoFallido(3, ahora);
        }

        long restantes = usuario.minutosRestantesBloqueo(ahora.plusMinutes(10), 30);
        assertTrue(restantes > 0);
        assertTrue(restantes <= 21);
    }

    @Test
    void deberiaRetornarCeroMinutosRestantesSiNoBloqueado() {
        assertEquals(0, usuario.minutosRestantesBloqueo(ahora, 30));
    }

    // --- Reinicio de intentos ---

    @Test
    void deberiaReiniciarContadorEnLoginExitoso() {
        usuario.registrarIntentoFallido(3, ahora);
        usuario.registrarIntentoFallido(3, ahora);
        assertEquals(2, usuario.getIntentosFallidos());

        usuario.reiniciarIntentosFallidos();
        assertEquals(0, usuario.getIntentosFallidos());
    }

    // --- Desbloqueo ---

    @Test
    void deberiaDesbloquearYReiniciarContador() {
        // Bloquear
        for (int i = 0; i < 3; i++) {
            usuario.registrarIntentoFallido(3, ahora);
        }
        assertEquals(EstadoUsuario.BLOQUEADO, usuario.getEstado());

        // Desbloquear
        usuario.desbloquear();

        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getFechaBloqueo());
    }

    // --- Constructor con password ---

    @Test
    void deberiaCrearUsuarioConPasswordHash() {
        assertNotNull(usuario.getPasswordHash());
        assertFalse(usuario.getPasswordHash().isEmpty());
        // El hash no debe ser el password en texto plano
        assertNotEquals("MiPassword123", usuario.getPasswordHash());
    }
}
