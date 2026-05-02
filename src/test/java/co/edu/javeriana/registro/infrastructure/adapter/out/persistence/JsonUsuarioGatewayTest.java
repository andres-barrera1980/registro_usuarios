package co.edu.javeriana.registro.infrastructure.adapter.out.persistence;

import co.edu.javeriana.registro.domain.model.Usuario;
import co.edu.javeriana.registro.domain.model.CodigoValidacion;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JsonUsuarioGatewayTest {

    private JsonUsuarioGateway gateway;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        String dbPath = tempDir.resolve("test-usuarios.json").toString();
        gateway = new JsonUsuarioGateway(dbPath);
    }

    @Test
    void deberiaGuardarYRecuperarUnUsuario() {
        Usuario usuario = new Usuario("1", "John Doe", "john@test.com");
        gateway.guardar(usuario);

        Optional<Usuario> recuperado = gateway.buscarPorEmail("john@test.com");
        
        assertTrue(recuperado.isPresent());
        assertEquals("John Doe", recuperado.get().getNombre());
        assertEquals(EstadoUsuario.NO_VERIFICADO, recuperado.get().getEstado());
    }

    @Test
    void deberiaActualizarUsuarioExistente() {
        Usuario usuario = new Usuario("1", "John Doe", "john@test.com");
        gateway.guardar(usuario);

        // Agregamos código (actualiza)
        Usuario elMismo = gateway.buscarPorEmail("john@test.com").get();
        elMismo.asignarNuevoCodigo(new CodigoValidacion("123", LocalDateTime.now().plusHours(2)));
        gateway.guardar(elMismo);

        Usuario recuperado = gateway.buscarPorEmail("john@test.com").get();
        assertNotNull(recuperado.getCodigoValidacionActivo());
        assertEquals("123", recuperado.getCodigoValidacionActivo().getCodigo());
    }

    @Test
    void deberiaEliminarUnUsuario() {
        Usuario usuario = new Usuario("1", "John Doe", "john@test.com");
        gateway.guardar(usuario);
        
        gateway.eliminar(usuario);
        
        Optional<Usuario> recuperado = gateway.buscarPorEmail("john@test.com");
        assertFalse(recuperado.isPresent());
    }
}
