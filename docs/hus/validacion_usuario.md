# Documento de Historias de Usuario: Verificación y Gestión de Cuentas

Este documento detalla las funcionalidades requeridas para el proceso de validación de identidad de nuevos usuarios, asegurando la integridad de la plataforma y el mantenimiento de la base de datos.

## 1. Emisión y Re-emisión de Verificación

Como usuario recién registrado,

quiero recibir un código de validación único en mi correo electrónico,

para demostrar que soy el propietario de la cuenta y poder activarla.

Criterios de Aceptación

AC-001: Tras completar el registro, el sistema debe enviar automáticamente un correo con un enlace o código de validación.

AC-002: El código de validación debe tener una vida útil de 24 horas. Pasado este tiempo, el código dejará de funcionar.

AC-003: Si el usuario solicita un nuevo correo de verificación, cualquier código enviado anteriormente debe quedar invalidado inmediatamente para garantizar la seguridad.

AC-004: El sistema debe confirmar al usuario que el nuevo correo ha sido enviado exitosamente.

## 2. Validación de Identidad y Activación de Cuenta

Como usuario con una cuenta "No Verificada",

quiero utilizar el código recibido para activar mi cuenta,

para acceder a las funciones de compra y venta de la plataforma.

Criterios de Aceptación

AC-001: Al ingresar un código válido y vigente, el sistema debe cambiar el estado de la cuenta a "Activo" de forma inmediata.

AC-002: Una vez activada la cuenta, el usuario debe ver un mensaje de éxito y ser dirigido a la pantalla de inicio de sesión.

AC-003: Si el usuario intenta usar un código que ya expiró, el sistema debe informar el motivo y ofrecer una opción clara para reenviar un código nuevo.

AC-004: Si un usuario con la cuenta ya activa intenta usar el código de nuevo, el sistema simplemente le informará que su cuenta ya está lista para usarse, sin mostrar mensajes de error técnico.

AC-005: Si un usuario intenta comprar o publicar un artículo sin estar verificado, el sistema debe impedir la acción y explicar que la verificación es obligatoria para esos procesos.

## 3. Limpieza Automática de Cuentas Inactivas

Como administrador de la plataforma,

quiero que las cuentas que nunca fueron verificadas se eliminen tras un periodo de tiempo,

para mantener la base de usuarios limpia y permitir que los correos electrónicos queden libres para nuevos registros.

Criterios de Aceptación

AC-001: El sistema debe identificar las cuentas que permanecen en estado "No Verificado" después de que su último código de validación haya expirado.

AC-002: Se debe establecer un periodo de gracia (ej. 7 días) después de la expiración del código antes de proceder a la eliminación definitiva de la cuenta.

AC-003: Una vez eliminada la cuenta por inactividad, el correo electrónico asociado debe quedar disponible para que cualquier persona pueda utilizarlo en un nuevo registro.

AC-004: El proceso de limpieza debe ser automático y no requerir intervención manual por parte del equipo administrativo.

Definición de "Terminado" (Definition of Done)

[ ] El flujo completo desde el registro hasta la activación ha sido probado y funciona sin errores.

[ ] Los mensajes mostrados al usuario son amigables y fáciles de entender.

[ ] Los bloqueos de seguridad para compras y ventas están operativos.

[ ] La anulación de códigos antiguos funciona correctamente al solicitar re-envíos.

[ ] El proceso de eliminación de cuentas obsoletas libera los correos electrónicos exitosamente.