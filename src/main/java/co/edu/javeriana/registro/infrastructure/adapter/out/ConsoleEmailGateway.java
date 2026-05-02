package co.edu.javeriana.registro.infrastructure.adapter.out;

import co.edu.javeriana.registro.application.gateway.EmailGateway;

public class ConsoleEmailGateway implements EmailGateway {

    @Override
    public void enviarCodigoValidacion(String emailDestino, String codigo) {
        System.out.println("[\uD83D\uDCE7 EMAIL SIMULADO] Enviando a: " + emailDestino);
        System.out.println("  Asunto: Tu código de validación");
        System.out.println("  Cuerpo: Usa el código " + codigo + " para activar tu cuenta.");
        System.out.println("---------------------------------------------------");
    }

    @Override
    public void enviarEnlaceRecuperacion(String emailDestino, String token) {
        System.out.println("[\uD83D\uDCE7 EMAIL SIMULADO] Enviando a: " + emailDestino);
        System.out.println("  Asunto: Recuperación de contraseña");
        System.out.println("  Cuerpo: Usa el siguiente token para recuperar tu contraseña: " + token);
        System.out.println("---------------------------------------------------");
    }

    @Override
    public void enviarConfirmacionCambioPassword(String emailDestino) {
        System.out.println("[\uD83D\uDCE7 EMAIL SIMULADO] Enviando a: " + emailDestino);
        System.out.println("  Asunto: Cambio de contraseña exitoso");
        System.out.println("  Cuerpo: Tu contraseña ha sido modificada correctamente. Las sesiones activas se han cerrado.");
        System.out.println("---------------------------------------------------");
    }
}
