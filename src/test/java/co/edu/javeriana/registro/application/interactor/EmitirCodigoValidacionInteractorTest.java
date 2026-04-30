package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmitirCodigoValidacionInteractorTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private EmailGateway emailGateway;

    private EmitirCodigoValidacionInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new EmitirCodigoValidacionInteractor(usuarioGateway, emailGateway);
    }

    @Test
    void deberiaEmitirCodigoParaUsuarioExistente() {
        Usuario usuario = new Usuario("1", "Andres", "andres@test.com", "hashedPw",
                EstadoUsuario.NO_VERIFICADO, LocalDateTime.now());
        when(usuarioGateway.buscarPorEmail("andres@test.com")).thenReturn(Optional.of(usuario));

        interactor.ejecutar("andres@test.com");

        verify(usuarioGateway).guardar(usuario);
        assertNotNull(usuario.getCodigoValidacionActivo());

        ArgumentCaptor<String> codigoCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailGateway).enviarCodigoValidacion(eq("andres@test.com"), codigoCaptor.capture());
        
        assertEquals(usuario.getCodigoValidacionActivo().getCodigo(), codigoCaptor.getValue());
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioNoExiste() {
        when(usuarioGateway.buscarPorEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> interactor.ejecutar("unknown@test.com"));

        verify(usuarioGateway, never()).guardar(any());
        verify(emailGateway, never()).enviarCodigoValidacion(anyString(), anyString());
    }
}
