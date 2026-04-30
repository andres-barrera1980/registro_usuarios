package co.edu.javeriana.registro.interfaces;

import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.application.interactor.ActivarCuentaUsuarioInteractor;
import co.edu.javeriana.registro.application.interactor.DesbloquearCuentaInteractor;
import co.edu.javeriana.registro.application.interactor.EmitirCodigoValidacionInteractor;
import co.edu.javeriana.registro.application.interactor.LimpiarCuentasInactivasInteractor;
import co.edu.javeriana.registro.application.interactor.LoginInteractor;
import co.edu.javeriana.registro.application.interactor.ReenviarTokenDesbloqueoInteractor;
import co.edu.javeriana.registro.domain.exception.CuentaBloqueadaException;
import co.edu.javeriana.registro.domain.exception.CuentaNoVerificadaException;
import co.edu.javeriana.registro.domain.exception.CuentaSuspendidaException;
import co.edu.javeriana.registro.domain.exception.CredencialesInvalidasException;
import co.edu.javeriana.registro.domain.exception.TokenExpiradoException;
import co.edu.javeriana.registro.domain.exception.TokenInvalidoException;
import co.edu.javeriana.registro.domain.model.ResultadoAutenticacion;
import co.edu.javeriana.registro.domain.model.Usuario;

import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class MenuConsola {

    private final UsuarioGateway usuarioGateway;
    private final EmitirCodigoValidacionInteractor emitirInteractor;
    private final ActivarCuentaUsuarioInteractor activarInteractor;
    private final LimpiarCuentasInactivasInteractor limpiarInteractor;
    private final LoginInteractor loginInteractor;
    private final DesbloquearCuentaInteractor desbloquearInteractor;
    private final ReenviarTokenDesbloqueoInteractor reenviarTokenInteractor;
    private final Scanner scanner;

    public MenuConsola(
            UsuarioGateway usuarioGateway,
            EmitirCodigoValidacionInteractor emitirInteractor,
            ActivarCuentaUsuarioInteractor activarInteractor,
            LimpiarCuentasInactivasInteractor limpiarInteractor,
            LoginInteractor loginInteractor,
            DesbloquearCuentaInteractor desbloquearInteractor,
            ReenviarTokenDesbloqueoInteractor reenviarTokenInteractor) {
        this.usuarioGateway = usuarioGateway;
        this.emitirInteractor = emitirInteractor;
        this.activarInteractor = activarInteractor;
        this.limpiarInteractor = limpiarInteractor;
        this.loginInteractor = loginInteractor;
        this.desbloquearInteractor = desbloquearInteractor;
        this.reenviarTokenInteractor = reenviarTokenInteractor;
        this.scanner = new Scanner(System.in);
    }

    public void mostrarMenu() {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Menú de Registro de Usuarios ---");
            System.out.println("1. Registrar nuevo usuario y emitir código");
            System.out.println("2. Activar cuenta de usuario");
            System.out.println("3. Iniciar sesión");
            System.out.println("4. Desbloquear cuenta con token");
            System.out.println("5. Reenviar enlace de desbloqueo");
            System.out.println("6. Ejecutar limpieza de cuentas inactivas");
            System.out.println("7. Salir");
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
                    iniciarSesion();
                    break;
                case "4":
                    desbloquearCuenta();
                    break;
                case "5":
                    reenviarEnlaceDesbloqueo();
                    break;
                case "6":
                    limpiarCuentas();
                    break;
                case "7":
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
        System.out.print("Ingrese contraseña: ");
        String password = scanner.nextLine();

        try {
            // Verificamos si existe
            if (usuarioGateway.buscarPorEmail(email).isPresent()) {
                System.out.println("El usuario con ese email ya existe. Reenviando código...");
            } else {
                Usuario nuevoUsuario = new Usuario(UUID.randomUUID().toString(), nombre, email, password);
                usuarioGateway.guardar(nuevoUsuario);
            }
            emitirInteractor.ejecutar(email);
            System.out.println("Registro completado y código emitido. Revise su correo para el código de validación.");
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
            System.out.println("¡Cuenta activada exitosamente! Ya puede iniciar sesión.");
        } catch (Exception e) {
            System.out.println("Error al activar: " + e.getMessage());
        }
    }

    private void iniciarSesion() {
        System.out.print("Ingrese email: ");
        String email = scanner.nextLine();
        System.out.print("Ingrese contraseña: ");
        String password = scanner.nextLine();

        try {
            ResultadoAutenticacion resultado = loginInteractor.ejecutar(email, password);
            System.out.println("¡" + resultado.getMensaje() + "!");
            System.out.println("Bienvenido, " + resultado.getUsuario().getNombre() + ".");
        } catch (CredencialesInvalidasException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (CuentaNoVerificadaException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Use la opción 1 para reenviar el código de verificación.");
        } catch (CuentaBloqueadaException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Puede desbloquear su cuenta con el token enviado a su correo (opción 4).");
            System.out.println("O solicitar un nuevo enlace de desbloqueo (opción 5).");
        } catch (CuentaSuspendidaException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
        }
    }

    private void desbloquearCuenta() {
        System.out.print("Ingrese el email de la cuenta a desbloquear: ");
        String email = scanner.nextLine();

        // Ayuda para el usuario: Mostrar el token si existe (conveniencia para pruebas)
        desbloquearInteractor.buscarUltimoToken(email).ifPresent(t -> {
            System.out.println("[AYUDA] Su token actual es: " + t);
        });

        System.out.print("Ingrese el token de desbloqueo recibido por email: ");
        String token = scanner.nextLine();

        try {
            desbloquearInteractor.ejecutar(email, token);
            System.out.println("¡Cuenta desbloqueada exitosamente! Ya puede iniciar sesión.");
        } catch (TokenExpiradoException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Solicite un nuevo enlace con la opción 5.");
        } catch (TokenInvalidoException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (CuentaSuspendidaException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error inesperado: " + e.getMessage());
        }
    }

    private void reenviarEnlaceDesbloqueo() {
        System.out.print("Ingrese el email de la cuenta bloqueada: ");
        String email = scanner.nextLine();

        try {
            reenviarTokenInteractor.ejecutar(email);
            System.out.println("Se ha enviado un nuevo enlace de desbloqueo a su correo.");
            System.out.println("El enlace anterior ha sido invalidado.");
        } catch (CuentaSuspendidaException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void limpiarCuentas() {
        System.out.println("Iniciando proceso de limpieza...");
        limpiarInteractor.ejecutar();
        System.out.println("Proceso completado.");
    }
}
