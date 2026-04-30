# 🔐 Historia de Usuario: Recuperación de Contraseña

## 🧾 Descripción

Como **buyer**, quiero poder recuperar mi contraseña a través de mi correo electrónico cuando la olvide, para poder volver a acceder a mi cuenta de forma segura.

> ⚠️ **Importante:** Esta funcionalidad solo aplica para usuarios con estado **activo**. Si la cuenta se encuentra en estado **bloqueado**, no será posible solicitar la recuperación de contraseña.

---

## ✅ Criterios de Aceptación

### 🔹 AC-001: Acceso a recuperación
Dado que el usuario está en la pantalla de inicio de sesión,  
cuando selecciona la opción **"¿Olvidaste tu contraseña?"**,  
entonces se muestra un formulario donde puede ingresar su correo y solicitar el enlace de recuperación.

---

### 🔹 AC-002: Solicitud con correo válido
Dado que el usuario ingresa un correo registrado y su cuenta está activa,  
cuando solicita la recuperación,  
entonces el sistema envía un enlace de recuperación al correo electrónico.  
El mensaje mostrado será general, indicando que si el correo existe, recibirá instrucciones.

---

### 🔹 AC-003: Solicitud con correo no registrado
Dado que el usuario ingresa un correo que no está registrado,  
cuando solicita la recuperación,  
entonces el sistema muestra el mismo mensaje general, sin indicar si el correo existe o no.

---

### 🔹 AC-004: Restablecimiento de contraseña
Dado que el usuario recibe un enlace válido,  
cuando accede a él y escribe una nueva contraseña válida,  
entonces el sistema actualiza su contraseña y le permite volver a iniciar sesión.

---

### 🔹 AC-005: Seguridad después del cambio
Dado que la contraseña fue cambiada exitosamente,  
cuando el proceso finaliza,  
entonces todas las sesiones activas del usuario se cierran y se envía una notificación por correo.

---

### 🔹 AC-006: Enlace expirado
Dado que el enlace de recuperación ha expirado,  
cuando el usuario intenta usarlo,  
entonces el sistema le informa que debe solicitar un nuevo enlace.

---

### 🔹 AC-007: Nueva solicitud de recuperación
Dado que el usuario ya solicitó un enlace previamente,  
cuando solicita uno nuevo,  
entonces el enlace anterior deja de ser válido y se genera uno nuevo.

---

### 🔹 AC-008: Usuario bloqueado
Dado que el usuario tiene su cuenta en estado **bloqueado**,  
cuando intenta solicitar la recuperación de contraseña,  
entonces el sistema no permite la solicitud e informa que debe contactar soporte o desbloquear su cuenta.

---

## 🏁 Definition of Done

- ✅ Funcionalidad implementada
- ✅ Pruebas realizadas y funcionando correctamente
- ✅ Envío de correo configurado
- ✅ Documentación actualizada
- ✅ Seguridad validada (enlaces temporales y cierre de sesiones)

---

## 📌 Notas adicionales

- El enlace de recuperación debe ser **temporal** (expira después de un tiempo definido).
- El sistema no debe revelar si un correo está registrado o no (por seguridad).
- El proceso debe garantizar la protección de la cuenta del usuario en todo momento.

---

✨ *Historia de usuario lista para ser incluida en documentación del proyecto (docs/HUS).*

