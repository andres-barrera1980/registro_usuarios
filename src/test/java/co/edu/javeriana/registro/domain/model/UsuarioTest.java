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

    // Stub simple para tests: el hash es simplemente "HASHED:" + rawPassword
    private final PasswordHasher stubHasher = new PasswordHasher() {
        @Override
        public String hash(String rawPassword) { return "HASHED:" + rawPassword; }
        @Override
        public boolean matches(String rawPassword, String hashedPassword) {
            return hashedPassword.equals("HASHED:" + rawPassword);
        }
    };

    @BeforeEach
    void setUp() {
        usuario = new Usuario("1234567890", "Andres", "andres@mail.com", "Passw0rd!", stubHasher);
        ahora = LocalDateTime.now();
    }

    @Test
    void deberiaTenerEstadoNoVerificadoAlCrear() {
        assertEquals(EstadoUsuario.NO_VERIFICADO, usuario.getEstado());
    }

    @Test
    void deberiaLanzarExcepcionConEmailInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Usuario("1234567890", "Andres", "correo-invalido", "Passw0rd!", stubHasher);
        });
        assertEquals("Por favor ingrese una dirección de correo electrónico válida", exception.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionConEmailInvalidoAlRehidratar() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Usuario("1234567890", "Andres", "correo-invalido", "hashedPw", EstadoUsuario.NO_VERIFICADO, LocalDateTime.now());
        });
        assertEquals("Por favor ingrese una dirección de correo electrónico válida", exception.getMessage());
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
    @Test
    void deberiaLanzarExcepcionConIdInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Usuario("123", "Andres", "andres@mail.com", "Passw0rd!", stubHasher);
        });
        assertEquals("El ID debe tener exactamente 10 caracteres numéricos", exception.getMessage());
        
        IllegalArgumentException exceptionLetras = assertThrows(IllegalArgumentException.class, () -> {
            new Usuario("12345ABCDE", "Andres", "andres@mail.com", "Passw0rd!", stubHasher);
        });
        assertEquals("El ID debe tener exactamente 10 caracteres numéricos", exceptionLetras.getMessage());
    }

    @Test
    void deberiaLanzarExcepcionConNombreInvalido() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Usuario("1234567890", "A", "andres@mail.com", "Passw0rd!", stubHasher);
        });
        assertEquals("El nombre debe tener al menos 2 caracteres", exception.getMessage());
    }
}
