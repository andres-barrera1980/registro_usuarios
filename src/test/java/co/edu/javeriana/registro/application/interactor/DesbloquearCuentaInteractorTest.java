package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.TokenDesbloqueoGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.exception.CuentaSuspendidaException;
import co.edu.javeriana.registro.domain.exception.TokenExpiradoException;
import co.edu.javeriana.registro.domain.exception.TokenInvalidoException;
import co.edu.javeriana.registro.domain.model.CodigoValidacion;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.TokenDesbloqueo;
import co.edu.javeriana.registro.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesbloquearCuentaInteractorTest {

    @Mock
    private TokenDesbloqueoGateway tokenGateway;

    @Mock
    private UsuarioGateway usuarioGateway;

    private DesbloquearCuentaInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new DesbloquearCuentaInteractor(tokenGateway, usuarioGateway);
    }

    private Usuario crearUsuarioBloqueado(String id) throws Exception {
        Usuario usuario = new Usuario(id, "Andres", "andres@test.com", "pass");
        CodigoValidacion codigo = new CodigoValidacion("CODE", LocalDateTime.now().plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        usuario.activarCuenta("CODE", LocalDateTime.now());
        // Bloquear
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(5, LocalDateTime.now());
        }
        return usuario;
    }

    @Test
    void deberiaDesbloquearConTokenValido() throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        TokenDesbloqueo token = new TokenDesbloqueo("token-abc", "1", ahora, ahora.plusHours(1), false, false);
        Usuario usuario = crearUsuarioBloqueado("1");

        when(tokenGateway.buscarPorToken("token-abc")).thenReturn(Optional.of(token));
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        interactor.ejecutar("andres@test.com", "token-abc");

        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        assertEquals(0, usuario.getIntentosFallidos());
        assertTrue(token.isUsado());
        verify(usuarioGateway).guardar(usuario);
        verify(tokenGateway).guardar(token);
    }

    @Test
    void deberiaRechazarTokenExpirado() {
        LocalDateTime ahora = LocalDateTime.now();
        TokenDesbloqueo token = new TokenDesbloqueo("token-abc", "1",
                ahora.minusHours(2), ahora.minusHours(1), false, false);

        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "pass");
        when(tokenGateway.buscarPorToken("token-abc")).thenReturn(Optional.of(token));
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(TokenExpiradoException.class, () -> interactor.ejecutar("andres@test.com", "token-abc"));
    }

    @Test
    void deberiaRechazarTokenYaUsado() {
        LocalDateTime ahora = LocalDateTime.now();
        TokenDesbloqueo token = new TokenDesbloqueo("token-abc", "1",
                ahora, ahora.plusHours(1), true, false);

        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "pass");
        when(tokenGateway.buscarPorToken("token-abc")).thenReturn(Optional.of(token));
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        TokenInvalidoException ex = assertThrows(TokenInvalidoException.class,
                () -> interactor.ejecutar("andres@test.com", "token-abc"));
        assertTrue(ex.getMessage().contains("ya fue utilizado"));
    }

    @Test
    void deberiaRechazarTokenRevocado() {
        LocalDateTime ahora = LocalDateTime.now();
        TokenDesbloqueo token = new TokenDesbloqueo("token-abc", "1",
                ahora, ahora.plusHours(1), false, true);

        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "pass");
        when(tokenGateway.buscarPorToken("token-abc")).thenReturn(Optional.of(token));
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        TokenInvalidoException ex = assertThrows(TokenInvalidoException.class,
                () -> interactor.ejecutar("andres@test.com", "token-abc"));
        assertTrue(ex.getMessage().contains("invalidado"));
    }

    @Test
    void deberiaRechazarTokenInexistente() {
        when(tokenGateway.buscarPorToken("no-existe")).thenReturn(Optional.empty());

        assertThrows(TokenInvalidoException.class, () -> interactor.ejecutar("andres@test.com", "no-existe"));
    }

    @Test
    void deberiaRechazarDesbloqueoSiCuentaSuspendida() throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        TokenDesbloqueo token = new TokenDesbloqueo("token-abc", "1",
                ahora, ahora.plusHours(1), false, false);

        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "pass");
        // Poner en estado SUSPENDIDO
        java.lang.reflect.Field estadoField = Usuario.class.getDeclaredField("estado");
        estadoField.setAccessible(true);
        estadoField.set(usuario, EstadoUsuario.SUSPENDIDO);

        when(tokenGateway.buscarPorToken("token-abc")).thenReturn(Optional.of(token));
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(CuentaSuspendidaException.class, () -> interactor.ejecutar("andres@test.com", "token-abc"));
    }
    @Test
    void deberiaRechazarSiElTokenNoPerteneceAlEmail() throws Exception {
        LocalDateTime ahora = LocalDateTime.now();
        // Token para usuario "1"
        TokenDesbloqueo token = new TokenDesbloqueo("token-abc", "1", ahora, ahora.plusHours(1), false, false);
        // Pero el email es de otro usuario ("2")
        Usuario otroUsuario = new Usuario("2", "Otro", "otro@test.com", "pass");

        when(tokenGateway.buscarPorToken("token-abc")).thenReturn(Optional.of(token));
        when(usuarioGateway.buscarPorEmail("otro@test.com")).thenReturn(Optional.of(otroUsuario));

        TokenInvalidoException ex = assertThrows(TokenInvalidoException.class, 
                () -> interactor.ejecutar("otro@test.com", "token-abc"));
        assertTrue(ex.getMessage().contains("no pertenece a esta cuenta"));
    }
}
