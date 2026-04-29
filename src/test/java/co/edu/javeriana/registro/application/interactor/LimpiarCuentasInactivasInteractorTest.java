package co.edu.javeriana.registro.application.interactor;

import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.domain.model.CodigoValidacion;
import co.edu.javeriana.registro.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LimpiarCuentasInactivasInteractorTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    private LimpiarCuentasInactivasInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new LimpiarCuentasInactivasInteractor(usuarioGateway);
    }

    @Test
    void deberiaEliminarSoloCuentasElegibles() {
        LocalDateTime ahora = LocalDateTime.now();
        
        Usuario elegible = new Usuario("1", "User1", "user1@test.com");
        elegible.asignarNuevoCodigo(new CodigoValidacion("123", ahora.minusDays(8)));
        
        Usuario noElegible = new Usuario("2", "User2", "user2@test.com");
        noElegible.asignarNuevoCodigo(new CodigoValidacion("456", ahora.minusDays(2)));
        
        List<Usuario> lista = Arrays.asList(elegible, noElegible);
        
        when(usuarioGateway.buscarCuentasInactivasExpiradas(any())).thenReturn(lista);

        interactor.ejecutar();

        verify(usuarioGateway).eliminar(elegible);
        verify(usuarioGateway, never()).eliminar(noElegible);
    }
}
