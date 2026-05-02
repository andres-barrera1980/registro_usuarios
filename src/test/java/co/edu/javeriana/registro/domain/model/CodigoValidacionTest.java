package co.edu.javeriana.registro.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class CodigoValidacionTest {

    @Test
    void deberiaEstarExpiradoSiLaFechaActualEsPosterior() {
        LocalDateTime ahora = LocalDateTime.now();
        CodigoValidacion codigo = new CodigoValidacion("ABC", ahora.minusMinutes(1));
        
        assertTrue(codigo.estaExpirado(ahora));
    }

    @Test
    void noDeberiaEstarExpiradoSiLaFechaActualEsAnterior() {
        LocalDateTime ahora = LocalDateTime.now();
        CodigoValidacion codigo = new CodigoValidacion("ABC", ahora.plusMinutes(1));
        
        assertFalse(codigo.estaExpirado(ahora));
    }

    @Test
    void generarNuevoDeberiaTenerExpiracionEn24Horas() {
        CodigoValidacion codigo = CodigoValidacion.generarNuevo();
        LocalDateTime ahora = LocalDateTime.now();
        
        // Verificamos que la expiración sea aproximadamente en 24 horas
        assertTrue(codigo.getFechaExpiracion().isAfter(ahora.plusHours(23)));
        assertTrue(codigo.getFechaExpiracion().isBefore(ahora.plusHours(25)));
    }
}
