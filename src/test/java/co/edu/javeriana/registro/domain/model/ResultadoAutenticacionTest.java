package co.edu.javeriana.registro.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultadoAutenticacionTest {

    @Test
    void deberiaCrearResultadoExitoso() {
        Usuario usuario = new Usuario("1", "Andres", "andres@mail.com", "pass123");
        ResultadoAutenticacion resultado = ResultadoAutenticacion.exitoso(usuario);

        assertTrue(resultado.isExitoso());
        assertEquals("Inicio de sesión exitoso", resultado.getMensaje());
        assertNotNull(resultado.getUsuario());
        assertEquals("Andres", resultado.getUsuario().getNombre());
    }

    @Test
    void deberiaCrearResultadoFallido() {
        ResultadoAutenticacion resultado = ResultadoAutenticacion.fallido("Email o contraseña incorrectos");

        assertFalse(resultado.isExitoso());
        assertEquals("Email o contraseña incorrectos", resultado.getMensaje());
        assertNull(resultado.getUsuario());
    }

    @Test
    void deberiaLanzarExcepcionSiUsuarioNuloEnExitoso() {
        assertThrows(NullPointerException.class, () -> ResultadoAutenticacion.exitoso(null));
    }
}
