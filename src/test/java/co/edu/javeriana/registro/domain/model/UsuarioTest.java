package co.edu.javeriana.registro.domain.model;

import co.edu.javeriana.registro.domain.exception.CodigoExpiradoException;
import co.edu.javeriana.registro.domain.exception.CodigoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario usuario;
    private LocalDateTime ahora;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("1", "Andres", "andres@mail.com");
        ahora = LocalDateTime.now();
    }

    @Test
    void deberiaTenerEstadoNoVerificadoAlCrear() {
        assertEquals(EstadoUsuario.NO_VERIFICADO, usuario.getEstado());
    }

    @Test
    void deberiaActivarCuentaConCodigoValido() {
        CodigoValidacion codigo = new CodigoValidacion("123456", ahora.plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        
        usuario.activarCuenta("123456", ahora);
        
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
        assertNull(usuario.getCodigoValidacionActivo());
    }

    @Test
    void deberiaLanzarExcepcionConCodigoInvalido() {
        CodigoValidacion codigo = new CodigoValidacion("123456", ahora.plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        
        assertThrows(CodigoInvalidoException.class, () -> usuario.activarCuenta("WRONG", ahora));
    }

    @Test
    void deberiaLanzarExcepcionYCambiarACaducadoCuandoCodigoVence() {
        CodigoValidacion codigo = new CodigoValidacion("123456", ahora.minusMinutes(1));
        usuario.asignarNuevoCodigo(codigo);
        
        assertThrows(CodigoExpiradoException.class, () -> usuario.activarCuenta("123456", ahora));
        assertEquals(EstadoUsuario.CADUCADO, usuario.getEstado());
    }

    @Test
    void noDeberiaHacerNadaSiLaCuentaYaEstaActiva() {
        CodigoValidacion codigo = new CodigoValidacion("123456", ahora.plusHours(24));
        usuario.asignarNuevoCodigo(codigo);
        usuario.activarCuenta("123456", ahora);
        
        // Intentar activar de nuevo
        usuario.activarCuenta("ANY", ahora);
        
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
    }

    @Test
    void deberiaSerElegibleParaLimpiezaSiPasaronSieteDiasDesdeExpiracion() {
        LocalDateTime expiracion = ahora.minusDays(8);
        CodigoValidacion codigo = new CodigoValidacion("123", expiracion);
        usuario.asignarNuevoCodigo(codigo);
        
        assertTrue(usuario.esElegibleParaLimpieza(ahora));
    }

    @Test
    void noDeberiaSerElegibleParaLimpiezaSiNoHanPasadoSieteDias() {
        LocalDateTime expiracion = ahora.minusDays(6);
        CodigoValidacion codigo = new CodigoValidacion("123", expiracion);
        usuario.asignarNuevoCodigo(codigo);
        
        assertFalse(usuario.esElegibleParaLimpieza(ahora));
    }
}
