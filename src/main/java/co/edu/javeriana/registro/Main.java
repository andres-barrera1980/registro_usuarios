package co.edu.javeriana.registro;

import co.edu.javeriana.registro.application.gateway.EmailGateway;
import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.application.interactor.ActivarCuentaUsuarioInteractor;
import co.edu.javeriana.registro.application.interactor.EmitirCodigoValidacionInteractor;
import co.edu.javeriana.registro.application.interactor.LimpiarCuentasInactivasInteractor;
import co.edu.javeriana.registro.domain.model.Usuario;

import java.time.LocalDateTime;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando aplicación de consola - Registro de Usuarios");

        // JPA file-based Gateway (SQLite)
        jakarta.persistence.EntityManagerFactory emf = jakarta.persistence.Persistence.createEntityManagerFactory("registro_usuarios_pu");
        UsuarioGateway persistenciaGateway = new co.edu.javeriana.registro.infrastructure.adapter.out.persistence.jpa.SqliteUsuarioGateway(emf);

        EmailGateway consolaEmailGateway = (emailDestino, codigo) -> 
            System.out.println("[EMAIL] Enviando a " + emailDestino + " el código de validación: " + codigo);

        // Inicializar interactors
        EmitirCodigoValidacionInteractor emitirInteractor = new EmitirCodigoValidacionInteractor(persistenciaGateway, consolaEmailGateway);
        ActivarCuentaUsuarioInteractor activarInteractor = new ActivarCuentaUsuarioInteractor(persistenciaGateway);
        LimpiarCuentasInactivasInteractor limpiarInteractor = new LimpiarCuentasInactivasInteractor(persistenciaGateway);

        // Inicializar Menu y mostrar
        co.edu.javeriana.registro.interfaces.MenuConsola menu = 
            new co.edu.javeriana.registro.interfaces.MenuConsola(
                persistenciaGateway, emitirInteractor, activarInteractor, limpiarInteractor);
        
        menu.mostrarMenu();
    }
}
