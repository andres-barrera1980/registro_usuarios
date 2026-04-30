package co.edu.javeriana.registro.interfaces;

import co.edu.javeriana.registro.application.gateway.UsuarioGateway;
import co.edu.javeriana.registro.application.interactor.ActivarCuentaUsuarioInteractor;
import co.edu.javeriana.registro.application.interactor.EmitirCodigoValidacionInteractor;
import co.edu.javeriana.registro.application.interactor.LimpiarCuentasInactivasInteractor;
import co.edu.javeriana.registro.domain.model.PasswordHasher;
import co.edu.javeriana.registro.domain.model.Usuario;

import java.util.Scanner;
import java.util.UUID;

public class MenuConsola {

    private final UsuarioGateway usuarioGateway;
    private final EmitirCodigoValidacionInteractor emitirInteractor;
    private final ActivarCuentaUsuarioInteractor activarInteractor;
    private final LimpiarCuentasInactivasInteractor limpiarInteractor;
    private final PasswordHasher passwordHasher;
    private final Scanner scanner;

    public MenuConsola(
            UsuarioGateway usuarioGateway,
            EmitirCodigoValidacionInteractor emitirInteractor,
            ActivarCuentaUsuarioInteractor activarInteractor,
            LimpiarCuentasInactivasInteractor limpiarInteractor,
            PasswordHasher passwordHasher) {
        this.usuarioGateway = usuarioGateway;
        this.emitirInteractor = emitirInteractor;
        this.activarInteractor = activarInteractor;
        this.limpiarInteractor = limpiarInteractor;
        this.passwordHasher = passwordHasher;
        this.scanner = new Scanner(System.in);
    }

    public void mostrarMenu() {
        boolean salir = false;

        while (!salir) {
            System.out.println("\n--- Menú de Registro de Usuarios ---");
            System.out.println("1. Registrar nuevo usuario y emitir código");
            System.out.println("2. Activar cuenta de usuario");
            System.out.println("3. Ejecutar limpieza de cuentas inactivas");
            System.out.println("4. Salir");
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
        System.out.print("Ingrese contraseña (mín. 8 chars, 1 mayúscula, 1 número, 1 especial): ");
        String password = scanner.nextLine();

        try {
            // Verificamos si existe
            if (usuarioGateway.buscarPorEmail(email).isPresent()) {
                System.out.println("El usuario con ese email ya existe. Reenviando código...");
            } else {
                String randomId = String.format("%010d", (long)(Math.random() * 10000000000L));
                Usuario nuevoUsuario = new Usuario(randomId, nombre, email, password, passwordHasher);
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
}
