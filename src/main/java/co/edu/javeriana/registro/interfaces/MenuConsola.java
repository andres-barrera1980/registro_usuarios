package co.edu.javeriana.registro.interfaces;

import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.application.interactor.ActivarCuentaUsuarioInteractor;
import co.edu.javeriana.registro.application.interactor.EmitirCodigoValidacionInteractor;
import co.edu.javeriana.registro.application.interactor.LimpiarCuentasInactivasInteractor;
import co.edu.javeriana.registro.domain.model.Usuario;

import java.util.Scanner;
import java.util.UUID;

public class MenuConsola {

    private final UsuarioGateway usuarioGateway;
    private final EmitirCodigoValidacionInteractor emitirInteractor;
    private final ActivarCuentaUsuarioInteractor activarInteractor;
    private final LimpiarCuentasInactivasInteractor limpiarInteractor;
    private final co.edu.javeriana.registro.application.interactor.SolicitarRecuperacionInteractor solicitarInteractor;
    private final co.edu.javeriana.registro.application.interactor.RestablecerPasswordInteractor restablecerInteractor;
    private final Scanner scanner;

    public MenuConsola(
            UsuarioGateway usuarioGateway,
            EmitirCodigoValidacionInteractor emitirInteractor,
            ActivarCuentaUsuarioInteractor activarInteractor,
            LimpiarCuentasInactivasInteractor limpiarInteractor,
            co.edu.javeriana.registro.application.interactor.SolicitarRecuperacionInteractor solicitarInteractor,
            co.edu.javeriana.registro.application.interactor.RestablecerPasswordInteractor restablecerInteractor) {
        this.usuarioGateway = usuarioGateway;
        this.emitirInteractor = emitirInteractor;
        this.activarInteractor = activarInteractor;
        this.limpiarInteractor = limpiarInteractor;
        this.solicitarInteractor = solicitarInteractor;
        this.restablecerInteractor = restablecerInteractor;
        this.scanner = new Scanner(System.in);
    }

    public void mostrarMenu() {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Menú de Registro de Usuarios ---");
            System.out.println("1. Registrar nuevo usuario y emitir código");
            System.out.println("2. Activar cuenta de usuario");
            System.out.println("3. Ejecutar limpieza de cuentas inactivas");
            System.out.println("4. Solicitar recuperación de contraseña");
            System.out.println("5. Restablecer contraseña (con token)");
            System.out.println("6. Salir");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    registrarUsuario();
                    break;
                case "2":
                    activarCuenta();
                    break;
                case "3":
                    limpiarCuentas();
                    break;
                case "4":
                    solicitarRecuperacion();
                    break;
                case "5":
                    restablecerPassword();
                    break;
                case "6":
                    salir = true;
                    System.out.println("Saliendo de la aplicación...");
                    break;
                default:
                    System.out.println("Opción no válida. Intente nuevamente.");
            }
        }
    }

    private void registrarUsuario() {
        System.out.print("Ingrese nombre: ");
        String nombre = scanner.nextLine();
        System.out.print("Ingrese email: ");
        String email = scanner.nextLine();

        try {
            // Verificamos si existe
            if (usuarioGateway.buscarPorEmail(email).isPresent()) {
                System.out.println("El usuario con ese email ya existe. Reenviando código...");
            } else {
                Usuario nuevoUsuario = new Usuario(UUID.randomUUID().toString(), nombre, email);
                usuarioGateway.guardar(nuevoUsuario);
            }
            emitirInteractor.ejecutar(email);
            System.out.println("Registro completado y código emitido.");
        } catch (Exception e) {
            System.out.println("Error al registrar: " + e.getMessage());
        }
    }

    private void activarCuenta() {
        System.out.print("Ingrese email de la cuenta a activar: ");
        String email = scanner.nextLine();
        System.out.print("Ingrese código de validación: ");
        String codigo = scanner.nextLine();

        try {
            activarInteractor.ejecutar(email, codigo);
            System.out.println("¡Cuenta activada exitosamente!");
        } catch (Exception e) {
            System.out.println("Error al activar: " + e.getMessage());
        }
    }

    private void limpiarCuentas() {
        System.out.println("Iniciando proceso de limpieza...");
        limpiarInteractor.ejecutar();
        System.out.println("Proceso completado.");
    }

    private void solicitarRecuperacion() {
        System.out.print("Ingrese su email para recuperar contraseña: ");
        String email = scanner.nextLine();
        
        System.out.println("Procesando solicitud...");
        solicitarInteractor.ejecutar(email);
        // Respuesta genérica de seguridad
        System.out.println("Si el correo existe en nuestro sistema y está activo, recibirá un enlace de recuperación pronto.");
    }

    private void restablecerPassword() {
        System.out.print("Ingrese su email: ");
        String email = scanner.nextLine();
        System.out.print("Ingrese el token de recuperación recibido por correo: ");
        String token = scanner.nextLine();
        System.out.print("Ingrese su nueva contraseña: ");
        String password = scanner.nextLine();

        try {
            restablecerInteractor.ejecutar(email, token, password);
            System.out.println("Contraseña restablecida exitosamente. Puede iniciar sesión con su nueva contraseña.");
        } catch (Exception e) {
            System.out.println("Error al restablecer contraseña: " + e.getMessage());
        }
    }
}
