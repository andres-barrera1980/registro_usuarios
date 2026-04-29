# Diseño Técnico: Verificación y Gestión de Cuentas

A continuación presento el diseño propuesto basado estrictamente en los principios de **Clean Architecture** y **Domain-Driven Design (DDD)**, extrayendo los requerimientos de las historias de usuario de `validacion_usuario.md`.

## 1. Identificación de Clases del Dominio (Entities & Value Objects)

El dominio encapsula las reglas de negocio críticas. Utilizaremos el patrón **Aggregate Root** centrado en el usuario.

*   **`Usuario` (Entity / Aggregate Root)**: Representa al usuario del sistema.
    *   *Atributos*: `id`, `email`, `estado` (Enum), `codigoValidacionActivo` (Value Object), `fechaRegistro`.
    *   *Comportamientos*: `asignarNuevoCodigo(CodigoValidacion)`, `activarCuenta(String codigoRecibido)`, `esElegibleParaLimpieza(fechaActual)`.
*   **`EstadoUsuario` (Enum)**: Representa los estados posibles de la cuenta.
    *   *Valores*: `NO_VERIFICADO`, `ACTIVO`, `CADUCADO`.
*   **`CodigoValidacion` (Value Object)**: Encapsula el código y su temporalidad.
    *   *Atributos*: `codigo` (String generado aleatoriamente), `fechaExpiracion` (LocalDateTime).
    *   *Comportamientos*: `estaExpirado(fechaActual)`, garantizando la regla de las 24 horas.
*   **Excepciones de Dominio**: `CuentaYaActivaException`, `CodigoExpiradoException`, `CodigoInvalidoException`, `CuentaNoVerificadaException` (para bloquear compras/ventas).

## 2. Interfaces con Sistemas Externos (Gateways / Application Interfaces)

En Clean Architecture, los Casos de Uso definen las interfaces que necesitan (Inversion of Dependency), y la Infraestructura las implementa. Evitamos la terminología de "Puertos" (propia de Arquitectura Hexagonal) y usamos Interfaces/Gateways.

*   **`UsuarioGateway` (Interface)**: Abstracción para interactuar con el almacenamiento de usuarios.
    *   `guardar(Usuario usuario)`
    *   `buscarPorEmail(String email)`
    *   `buscarCuentasInactivasExpiradas(LocalDateTime fechaCorte)`
    *   `eliminar(Usuario usuario)`
*   **`EmailGateway` (Interface)**: Abstracción para enviar comunicaciones.
    *   `enviarCodigoValidacion(String emailDestino, String codigo)`

## 3. Clases de Casos de Uso (Use Case Interactors)

Los interactuadores de casos de uso orquestan el flujo, interactuando con las Entidades y utilizando las Interfaces definidas arriba.

*   **`EmitirCodigoValidacionInteractor`**: Orquesta la creación de un nuevo código en la entidad `Usuario`, persiste usando `UsuarioGateway` e invoca `EmailGateway`. Invalida códigos anteriores automáticamente al asignar uno nuevo en la entidad.
*   **`ActivarCuentaUsuarioInteractor`**: Recupera el usuario por email usando `UsuarioGateway`, invoca `usuario.activarCuenta(codigo)` e informa éxito o error.
*   **`LimpiarCuentasInactivasInteractor`**: Un proceso (job) que busca usuarios en estado `NO_VERIFICADO` o `CADUCADO` cuyos códigos hayan expirado hace más de 7 días, usando el gateway, y los elimina.

## 4. Entrada a la Aplicación (Controllers)

*   **`Main` (Console App)**: Actuará como el controlador principal de entrada por ahora. Instanciará las dependencias (inyectando implementaciones en memoria o mockeadas de los Gateways a los Interactors) y simulará la interacción del usuario a través de la consola para probar el registro y la validación.

## 5. Plan de Pruebas Unitarias del Modelo (DDD)

Las pruebas unitarias se enfocarán en el Aggregate Root (`Usuario`) y el Value Object (`CodigoValidacion`), utilizando **JUnit 5**.

*   **`CodigoValidacionTest`**:
    *   Debería identificar correctamente si un código está expirado basado en la fecha de expiración.
    *   Debería instanciarse correctamente asegurando una validez de 24 horas.
*   **`UsuarioTest`**:
    *   Al registrar un usuario, su estado inicial debe ser `NO_VERIFICADO`.
    *   Debería permitir agregar un nuevo `CodigoValidacion` e invalidar tácitamente el anterior al sobrescribirlo.
    *   `activarCuenta(codigo)` debería cambiar el estado a `ACTIVO` si el código es correcto y no está expirado.
    *   `activarCuenta(codigo)` debería lanzar `CodigoExpiradoException` y transicionar a estado `CADUCADO` si han pasado más de 24 horas.
    *   `activarCuenta(codigo)` no debería afectar si la cuenta ya estaba en estado `ACTIVO`.
    *   `esElegibleParaLimpieza` debería retornar `true` solo si han pasado los 7 días de gracia desde la expiración del código.

---

## Tareas de Implementación

- [x] Crear el Enum `EstadoUsuario` (NO_VERIFICADO, ACTIVO, CADUCADO).
- [x] Crear Value Object `CodigoValidacion`.
- [x] Crear Entity `Usuario` (Aggregate Root) y sus excepciones.
- [x] Escribir los tests unitarios (`UsuarioTest`, `CodigoValidacionTest`) aplicando TDD.
- [x] Definir las interfaces `UsuarioGateway` y `EmailGateway`.
- [x] Crear los Interactors (`EmitirCodigoValidacionInteractor`, `ActivarCuentaUsuarioInteractor`, `LimpiarCuentasInactivasInteractor`).
- [x] Crear clase `Main` para la ejecución y pruebas de consola.
