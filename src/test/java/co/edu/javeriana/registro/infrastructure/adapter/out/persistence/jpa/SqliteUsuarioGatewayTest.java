package co.edu.javeriana.registro.infrastructure.adapter.out.persistence.jpa;

import co.edu.javeriana.registro.domain.model.CodigoValidacion;
import co.edu.javeriana.registro.domain.model.EstadoUsuario;
import co.edu.javeriana.registro.domain.model.Usuario;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SqliteUsuarioGatewayTest {

    private EntityManagerFactory emf;
    private SqliteUsuarioGateway gateway;

    @BeforeEach
    void setUp() {
        Map<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", "jdbc:sqlite::memory:");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");

        emf = Persistence.createEntityManagerFactory("registro_usuarios_pu", properties);
        gateway = new SqliteUsuarioGateway(emf);
    }

    @AfterEach
    void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    void deberiaGuardarYRecuperarUnUsuario() {
        Usuario usuario = new Usuario("1", "Jane Doe", "jane@test.com");
        gateway.guardar(usuario);

        Optional<Usuario> recuperado = gateway.buscarPorEmail("jane@test.com");
        
        assertTrue(recuperado.isPresent());
        assertEquals("Jane Doe", recuperado.get().getNombre());
        assertEquals(EstadoUsuario.NO_VERIFICADO, recuperado.get().getEstado());
    }

    @Test
    void deberiaActualizarUsuarioExistente() {
        Usuario usuario = new Usuario("1", "Jane Doe", "jane@test.com");
        gateway.guardar(usuario);

        Usuario elMismo = gateway.buscarPorEmail("jane@test.com").get();
        elMismo.asignarNuevoCodigo(new CodigoValidacion("123", LocalDateTime.now().plusHours(2)));
        gateway.guardar(elMismo);

        Usuario recuperado = gateway.buscarPorEmail("jane@test.com").get();
        assertNotNull(recuperado.getCodigoValidacionActivo());
        assertEquals("123", recuperado.getCodigoValidacionActivo().getCodigo());
    }

    @Test
    void deberiaEliminarUnUsuario() {
        Usuario usuario = new Usuario("1", "Jane Doe", "jane@test.com");
        gateway.guardar(usuario);
        
        gateway.eliminar(usuario);
        
        Optional<Usuario> recuperado = gateway.buscarPorEmail("jane@test.com");
        assertFalse(recuperado.isPresent());
    }
}
