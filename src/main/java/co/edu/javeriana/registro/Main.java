package co.edu.javeriana.registro;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.TokenDesbloqueoGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.application.interactor.ActivarCuentaUsuarioInteractor;
import co.edu.javeriana.registro.application.interactor.DesbloquearCuentaInteractor;
import co.edu.javeriana.registro.application.interactor.EmitirCodigoValidacionInteractor;
import co.edu.javeriana.registro.application.interactor.LimpiarCuentasInactivasInteractor;
import co.edu.javeriana.registro.application.interactor.LoginInteractor;
import co.edu.javeriana.registro.application.interactor.ReenviarTokenDesbloqueoInteractor;
import co.edu.javeriana.registro.infrastructure.adapter.out.persistence.jpa.SqliteTokenDesbloqueoGateway;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando aplicación de consola - Registro de Usuarios");

        // JPA file-based Gateway (SQLite)
        jakarta.persistence.EntityManagerFactory emf = jakarta.persistence.Persistence.createEntityManagerFactory("registro_usuarios_pu");
        UsuarioGateway persistenciaGateway = new co.edu.javeriana.registro.infrastructure.adapter.out.persistence.jpa.SqliteUsuarioGateway(emf);
        TokenDesbloqueoGateway tokenGateway = new SqliteTokenDesbloqueoGateway(emf);

        // Email Gateway — imprime en consola (simulación)
        EmailGateway consolaEmailGateway = new EmailGateway() {
            @Override
            public void enviarCodigoValidacion(String emailDestino, String codigo) {
                System.out.println("[EMAIL] Enviando a " + emailDestino + " el código de validación: " + codigo);
            }

            @Override
            public void enviarNotificacionBloqueo(String emailDestino, String nombreUsuario, LocalDateTime horaBloqueo) {
                System.out.println("[EMAIL] Notificación de bloqueo para " + nombreUsuario + " (" + emailDestino + ")");
                System.out.println("  → Hora del bloqueo: " + horaBloqueo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                System.out.println("  → Su cuenta ha sido bloqueada por actividad sospechosa.");
                System.out.println("  → Use el token de desbloqueo enviado a continuación para recuperar su acceso.");
            }

            @Override
            public void enviarEnlaceDesbloqueo(String emailDestino, String nombreUsuario, String token, LocalDateTime expiracion) {
                System.out.println("[EMAIL] Enlace de desbloqueo para " + nombreUsuario + " (" + emailDestino + ")");
                System.out.println("  → Token: " + token);
                System.out.println("  → Válido hasta: " + expiracion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                System.out.println("  → Use este token en la opción 4 del menú para desbloquear su cuenta.");
            }
        };

        // Inicializar interactors
        EmitirCodigoValidacionInteractor emitirInteractor = new EmitirCodigoValidacionInteractor(persistenciaGateway, consolaEmailGateway);
        ActivarCuentaUsuarioInteractor activarInteractor = new ActivarCuentaUsuarioInteractor(persistenciaGateway);
        LimpiarCuentasInactivasInteractor limpiarInteractor = new LimpiarCuentasInactivasInteractor(persistenciaGateway);
        LoginInteractor loginInteractor = new LoginInteractor(persistenciaGateway, consolaEmailGateway, tokenGateway);
        DesbloquearCuentaInteractor desbloquearInteractor = new DesbloquearCuentaInteractor(tokenGateway, persistenciaGateway);
        ReenviarTokenDesbloqueoInteractor reenviarTokenInteractor = new ReenviarTokenDesbloqueoInteractor(persistenciaGateway, tokenGateway, consolaEmailGateway);

        // Inicializar Menu y mostrar
        co.edu.javeriana.registro.interfaces.MenuConsola menu = 
            new co.edu.javeriana.registro.interfaces.MenuConsola(
                persistenciaGateway, emitirInteractor, activarInteractor, limpiarInteractor,
                loginInteractor, desbloquearInteractor, reenviarTokenInteractor);
        
        menu.mostrarMenu();
    }
}
