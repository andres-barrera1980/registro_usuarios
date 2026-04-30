package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.TokenDesbloqueoGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.exception.CuentaSuspendidaException;
import co.edu.javeriana.registro.domain.model.CodigoValidacion;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReenviarTokenDesbloqueoInteractorTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private TokenDesbloqueoGateway tokenGateway;

    @Mock
    private EmailGateway emailGateway;

    private ReenviarTokenDesbloqueoInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new ReenviarTokenDesbloqueoInteractor(usuarioGateway, tokenGateway, emailGateway);
    }

    private Usuario crearUsuarioBloqueado() {
        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "pass");
        CodigoValidacion codigo = new CodigoValidacion("CODE", LocalDateTime.now().plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        usuario.activarCuenta("CODE", LocalDateTime.now());
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(5, LocalDateTime.now());
        }
        return usuario;
    }

    @Test
    void deberiaRevocarTokenAnteriorYEnviarNuevo() {
        Usuario usuario = crearUsuarioBloqueado();
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        interactor.ejecutar("andres@test.com");

        verify(tokenGateway).revocarTokensDeUsuario("1");
        verify(tokenGateway).guardar(any());
        verify(emailGateway).enviarEnlaceDesbloqueo(eq("andres@test.com"), eq("Andres"), any(), any());
    }

    @Test
    void deberiaRechazarSiCuentaNoEstaBloqueada() {
        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "pass");
        CodigoValidacion codigo = new CodigoValidacion("CODE", LocalDateTime.now().plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        usuario.activarCuenta("CODE", LocalDateTime.now());
        // Estado ACTIVO, no bloqueado
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(IllegalStateException.class, () -> interactor.ejecutar("andres@test.com"));
        verify(tokenGateway, never()).guardar(any());
    }

    @Test
    void deberiaRechazarSiCuentaSuspendida() throws Exception {
        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "pass");
        java.lang.reflect.Field estadoField = Usuario.class.getDeclaredField("estado");
        estadoField.setAccessible(true);
        estadoField.set(usuario, EstadoUsuario.SUSPENDIDO);

        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(CuentaSuspendidaException.class, () -> interactor.ejecutar("andres@test.com"));
        verify(tokenGateway, never()).guardar(any());
    }
}
