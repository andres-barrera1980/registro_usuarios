package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.TokenDesbloqueoGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.exception.CuentaBloqueadaException;
import co.edu.javeriana.registro.domain.exception.CuentaNoVerificadaException;
import co.edu.javeriana.registro.domain.exception.CuentaSuspendidaException;
import co.edu.javeriana.registro.domain.exception.CredencialesInvalidasException;
import co.edu.javeriana.registro.domain.model.CodigoValidacion;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.ResultadoAutenticacion;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginInteractorTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private EmailGateway emailGateway;

    @Mock
    private TokenDesbloqueoGateway tokenGateway;

    private LoginInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new LoginInteractor(usuarioGateway, emailGateway, tokenGateway);
    }

    private Usuario crearUsuarioActivo(String email, String password) {
        Usuario usuario = new Usuario("1", "Andres", email, password);
        CodigoValidacion codigo = new CodigoValidacion("CODE", LocalDateTime.now().plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        usuario.activarCuenta("CODE", LocalDateTime.now());
        return usuario;
    }

    // --- AC-001: Login exitoso ---

    @Test
    void deberiaAutenticarConCredencialesValidas() {
        Usuario usuario = crearUsuarioActivo("andres@test.com", "MiPass123");
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        ResultadoAutenticacion resultado = interactor.ejecutar("andres@test.com", "MiPass123");

        assertTrue(resultado.isExitoso());
        assertNotNull(resultado.getUsuario());
        assertEquals(0, resultado.getUsuario().getIntentosFallidos());
        verify(usuarioGateway).guardar(usuario);
    }

    // --- AC-002: Credenciales incorrectas ---

    @Test
    void deberiaRechazarConPasswordIncorrecto() {
        Usuario usuario = crearUsuarioActivo("andres@test.com", "MiPass123");
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(CredencialesInvalidasException.class,
                () -> interactor.ejecutar("andres@test.com", "PasswordIncorrecto"));

        assertEquals(1, usuario.getIntentosFallidos());
        verify(usuarioGateway).guardar(usuario);
    }

    @Test
    void deberiaRechazarEmailInexistenteConMensajeGenerico() {
        when(usuarioGateway.buscarPorEmail("noexiste@test.com")).thenReturn(Optional.empty());

        CredencialesInvalidasException ex = assertThrows(CredencialesInvalidasException.class,
                () -> interactor.ejecutar("noexiste@test.com", "cualquiera"));

        assertEquals("Email o contraseña incorrectos", ex.getMessage());
    }

    // --- Bloqueo tras 3 intentos (HU-AUTH-002 AC-001) ---

    @Test
    void deberiaBloquearTrasTresIntentosFallidos() {
        Usuario usuario = crearUsuarioActivo("andres@test.com", "MiPass123");
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        // 2 intentos fallidos
        for (int i = 0; i < 2; i++) {
            assertThrows(CredencialesInvalidasException.class,
                    () -> interactor.ejecutar("andres@test.com", "Wrong"));
        }

        // 3er intento → bloqueo
        assertThrows(CuentaBloqueadaException.class,
                () -> interactor.ejecutar("andres@test.com", "Wrong"));

        assertEquals(EstadoUsuario.BLOQUEADO, usuario.getEstado());
        verify(emailGateway).enviarNotificacionBloqueo(eq("andres@test.com"), eq("Andres"), any());
        verify(tokenGateway).guardar(any());
        verify(emailGateway).enviarEnlaceDesbloqueo(eq("andres@test.com"), eq("Andres"), any(), any());
    }

    // --- AC-004: Intento durante bloqueo ---

    @Test
    void deberiaRechazarLoginDeCuentaBloqueada() {
        Usuario usuario = crearUsuarioActivo("andres@test.com", "MiPass123");
        // Simular bloqueo
        for (int i = 0; i < 3; i++) {
            usuario.registrarIntentoFallido(3, LocalDateTime.now());
        }
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        CuentaBloqueadaException ex = assertThrows(CuentaBloqueadaException.class,
                () -> interactor.ejecutar("andres@test.com", "MiPass123"));

        assertTrue(ex.getMinutosRestantes() > 0);
    }

    // --- AC-003: Desbloqueo automático por tiempo ---

    @Test
    void deberiaPermitirLoginTrasDesbloqueoAutomatico() throws Exception {
        Usuario usuario = crearUsuarioActivo("andres@test.com", "MiPass123");

        // Simular bloqueo ocurrido hace 31 minutos
        java.lang.reflect.Field estadoField = Usuario.class.getDeclaredField("estado");
        estadoField.setAccessible(true);
        estadoField.set(usuario, EstadoUsuario.BLOQUEADO);

        java.lang.reflect.Field fechaBloqueoField = Usuario.class.getDeclaredField("fechaBloqueo");
        fechaBloqueoField.setAccessible(true);
        fechaBloqueoField.set(usuario, LocalDateTime.now().minusMinutes(31));

        java.lang.reflect.Field intentosField = Usuario.class.getDeclaredField("intentosFallidos");
        intentosField.setAccessible(true);
        intentosField.set(usuario, 3);

        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        ResultadoAutenticacion resultado = interactor.ejecutar("andres@test.com", "MiPass123");

        assertTrue(resultado.isExitoso());
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
    }

    // --- AC-003 Login: Cuenta no verificada ---

    @Test
    void deberiaRechazarCuentaNoVerificada() {
        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "MiPass123");
        // Estado por defecto: NO_VERIFICADO
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(CuentaNoVerificadaException.class,
                () -> interactor.ejecutar("andres@test.com", "MiPass123"));
    }

    // --- AC-004 Login: Cuenta suspendida ---

    @Test
    void deberiaRechazarCuentaSuspendida() throws Exception {
        Usuario usuario = crearUsuarioActivo("andres@test.com", "MiPass123");
        // Cambiar estado a SUSPENDIDO
        java.lang.reflect.Field estadoField = Usuario.class.getDeclaredField("estado");
        estadoField.setAccessible(true);
        estadoField.set(usuario, EstadoUsuario.SUSPENDIDO);

        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(CuentaSuspendidaException.class,
                () -> interactor.ejecutar("andres@test.com", "MiPass123"));
    }

    // --- AC-005 Bloqueo: Reinicio de contador en login exitoso ---

    @Test
    void deberiaReiniciarContadorEnLoginExitoso() {
        Usuario usuario = crearUsuarioActivo("andres@test.com", "MiPass123");
        // Simular 2 intentos fallidos previos
        usuario.registrarIntentoFallido(3, LocalDateTime.now());
        usuario.registrarIntentoFallido(3, LocalDateTime.now());

        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        ResultadoAutenticacion resultado = interactor.ejecutar("andres@test.com", "MiPass123");

        assertTrue(resultado.isExitoso());
        assertEquals(0, resultado.getUsuario().getIntentosFallidos());
    }
}
