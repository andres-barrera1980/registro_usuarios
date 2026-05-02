# Diseño Técnico: Recuperación de Contraseña

Este documento describe el plan de implementación para la funcionalidad de **Recuperación de Contraseña**, permitiendo a los usuarios con estado **Activo** restablecer su acceso mediante un token temporal enviado por correo electrónico, cumpliendo con los Criterios de Aceptación (AC) de la historia de usuario. El diseño se basa en **Clean Architecture**, **Domain-Driven Design (DDD)** y **SOLID**.

---

## 1. Análisis y Modelado de Dominio

El proceso de recuperación se basa en la generación de un **Token de Recuperación** vinculado a un **Usuario**. Este token es efímero y debe invalidarse tras su uso o ante una nueva solicitud para garantizar la seguridad del proceso.

### 1.1 Enumeraciones necesarias (Enums)
* **EstadoUsuario:** Valores `ACTIVO`, `BLOQUEADO`.
    * *Regla de Negocio:* Solo el estado `ACTIVO` permite iniciar el flujo de recuperación.

### 1.2 `TokenRecuperacion` (Value Object / Entity)
Atributos que garantizan la seguridad y temporalidad:
* **token** (String/UUID): Identificador único y difícil de adivinar.
* **fechaExpiracion** (LocalDateTime): Define el límite de validez del enlace.
* **usado** (boolean): Marca si el token ya cumplió su ciclo para evitar reutilización.

**Reglas de Negocio en el Dominio:**
* **RN-REC-001 (Expiración):** Método `esValido()` que verifica que el token no haya expirado y no haya sido usado previamente (AC-006).
* **RN-REC-002 (Unicidad):** Al generar un nuevo token, cualquier token anterior del usuario pierde validez automáticamente (AC-007).

### 1.3 `Usuario` (Aggregate Root)
* **Atributos:** `password`, `email`, `estado`.
* **Comportamientos:**
    * `public TokenRecuperacion generarTokenRecuperacion()`: Valida que el estado sea `ACTIVO`. Si está `BLOQUEADO`, lanza una excepción de negocio que impide la solicitud (AC-008).
    * `public void restablecerPassword(String nuevoPassword, TokenRecuperacion token)`: Valida la integridad del token, actualiza la credencial y activa el cierre de todas las sesiones abiertas (AC-004, AC-005).

---

## 2. Capa de Aplicación (Use Cases)

Se implementarán dos interactores para separar la solicitud de la ejecución del cambio:

1.  **SolicitarRecuperacionInteractor:**
    * **Flujo:** Recibe el correo del usuario. Busca al usuario en el `UsuarioGateway`.
    * **Seguridad (AC-002, AC-003):** Independientemente de si el correo existe o si la cuenta está bloqueada, el interactor siempre confirma el envío de instrucciones para no revelar información sensible.
    * **Acción:** Si el usuario es elegible, genera el token, persiste el cambio y dispara el servicio de notificación por correo.

2.  **RestablecerPasswordInteractor:**
    * **Flujo:** Recibe el token y la nueva contraseña. Valida la validez del token a través del dominio.
    * **Acción:** Actualiza la contraseña en el sistema, marca el token como usado y gatilla la notificación de éxito al correo del usuario (AC-005).

---

## 3. Capa de Infraestructura (Gateways y Persistencia)

### 3.1 Base de Datos (JPA / SQL):
* **@Embeddable `TokenRecuperacionJpaEmbeddable`**: Columnas `recovery_token`, `recovery_expiration` y `recovery_used`.
* **Mappers:** Actualizar los mappers en `SqliteUsuarioGateway` para convertir el objeto `TokenRecuperacion` entre el dominio y la base de datos.

### 3.2 Notificaciones (Email Gateway):
* **EmailService:** Implementación encargada de construir y enviar el mensaje con el enlace temporal al usuario.

---

## 4. Adaptadores de Entrada (UI / Consola)

Se incorporarán las siguientes opciones al flujo del sistema:
* **Recuperar Contraseña:** Solicita el correo y llama a `SolicitarRecuperacionInteractor`. Muestra mensaje general de éxito.
* **Ingreso de Nueva Contraseña:** Simula el clic en el enlace del correo, pide la nueva contraseña y llama a `RestablecerPasswordInteractor`.

---

## 5. Plan de Pruebas Unitarias e Integración (TDD)

* **Dominio:** `TokenRecuperacionTest` para validar que los tokens expiren correctamente según el tiempo definido.
* **Aplicación:** Mockear el `UsuarioGateway` para verificar que `SolicitarRecuperacionInteractor` maneje correos inexistentes y usuarios bloqueados sin filtrar información de seguridad.
* **Infraestructura:** Pruebas de persistencia para asegurar que tras el cambio de contraseña, el token se marque como "usado" en la base de datos.

---

## 📋 Tareas de Implementación (Checklist)

* [ ] 1. Crear el Value Object `TokenRecuperacion` con lógica de expiración.
* [ ] 2. Actualizar `Usuario` para incluir el estado y la lógica de validación para bloqueo (AC-008).
* [ ] 3. Implementar `SolicitarRecuperacionInteractor` con respuesta general de seguridad (AC-003).
* [ ] 4. Implementar `RestablecerPasswordInteractor` asegurando el cierre de sesiones (AC-005).
* [ ] 5. Crear `TokenRecuperacionJpaEmbeddable` y actualizar la entidad `UsuarioJpaEntity`.
* [ ] 6. Configurar el servicio de envío de correos (EmailGateway).
* [ ] 7. Realizar pruebas de integración de flujo completo: solicitud -> expiración -> restablecimiento.