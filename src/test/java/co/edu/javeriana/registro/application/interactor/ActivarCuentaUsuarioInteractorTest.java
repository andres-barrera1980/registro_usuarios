package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.exception.CodigoInvalidoException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivarCuentaUsuarioInteractorTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    private ActivarCuentaUsuarioInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new ActivarCuentaUsuarioInteractor(usuarioGateway);
    }

    @Test
    void deberiaActivarCuentaCorrectamente() {
        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "hashedPw",
                EstadoUsuario.NO_VERIFICADO, LocalDateTime.now());
        CodigoValidacion codigo = new CodigoValidacion("123456", LocalDateTime.now().plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        interactor.ejecutar("andres@test.com", "123456");

        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        verify(usuarioGateway).guardar(usuario);
    }

    @Test
    void deberiaLanzarExcepcionConCodigoInvalido() {
        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "hashedPw",
                EstadoUsuario.NO_VERIFICADO, LocalDateTime.now());
        CodigoValidacion codigo = new CodigoValidacion("123456", LocalDateTime.now().plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        assertThrows(CodigoInvalidoException.class, () -> interactor.ejecutar("andres@test.com", "WRONG"));
        
        verify(usuarioGateway, never()).guardar(any());
    }
}
