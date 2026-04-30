# Historia de Usuario: Editar Perfil del Comprador

**Como** comprador, **quiero** poder ver y editar la información de mi perfil (teléfono, edad, dirección, género y avatar), **para** personalizar mi presencia en la plataforma y mantener mis datos complementarios actualizados.

---

## Criterios de Aceptación

**AC-001 – Ver mi perfil**
Dado que estoy autenticado en la aplicación, cuando ingreso a la sección de mi perfil, entonces puedo ver mi número de teléfono, edad, dirección, género y avatar.

**AC-002 – Editar mis datos de perfil**
Dado que estoy autenticado, cuando modifico uno o más campos de mi perfil y guardo los cambios, entonces el sistema actualiza la información correctamente y me muestra un mensaje de confirmación.

**AC-003 – Selección de avatar**
Dado que estoy editando mi perfil, cuando accedo al campo de avatar, entonces el sistema me muestra los siguientes íconos para elegir como representación visual:

| Valor | Ícono |
|---|---|
| STAR | ⭐ |
| MOON | 🌙 |
| SUN | ☀️ |
| HEART | ♥ |
| DIAMOND | ◆ |
| BOLT | ⚡ |
| LEAF | 🍃 |
| CROWN | ♛ |
| FLAME | 🔥 |
| GHOST | ✦ |

Al seleccionar uno y guardar, el avatar queda asociado a mi perfil. Si no selecciono ninguno, el sistema asigna ⭐ como avatar por defecto.

**AC-004 – Validación del número de teléfono**
Dado que estoy editando mi perfil, cuando ingreso un número de teléfono con formato inválido (letras o menos de 7 dígitos), entonces el sistema me muestra el mensaje: *"El número de teléfono no es válido. Ingresa solo dígitos y verifica que tenga el formato correcto."*

**AC-005 – Validación de la edad**
Dado que estoy editando mi perfil, cuando ingreso un valor que no corresponde a un número entero positivo mayor a cero, entonces el sistema me muestra el mensaje: *"Por favor ingresa una edad válida."*

**AC-006 – Selección de género**
Dado que estoy editando mi perfil, cuando accedo al campo de género, entonces el sistema me presenta las opciones: Masculino, Femenino y Prefiero no decirlo. Si no selecciono ninguna, el sistema registra automáticamente *Prefiero no decirlo*.

**AC-007 – Validación de dirección**
Dado que estoy editando mi perfil, cuando intento guardar los cambios con el campo de dirección vacío, entonces el sistema me muestra el mensaje: *"Por favor ingresa una dirección válida."*

**AC-008 – Guardar con campos obligatorios vacíos**
Dado que estoy editando mi perfil, cuando intento guardar dejando un campo obligatorio sin completar, entonces el sistema bloquea el guardado e indica cuál campo debe completarse.

---

## Reglas de Negocio

- **RN-PROF-001:** El perfil es independiente del usuario. Nombre, correo y contraseña no se gestionan desde el perfil.
- **RN-PROF-002:** El número de teléfono debe contener solo dígitos y tener mínimo 7 caracteres.
- **RN-PROF-003:** La edad es una fecha, que tiene que tener como minimo 14 años.
- **RN-PROF-004:** El género es opcional. Sus valores posibles son: Masculino, Femenino y Prefiero no decirlo. Si no se selecciona, se registra *Prefiero no decirlo* por defecto.
- **RN-PROF-005:** La dirección es obligatoria. No puede guardarse vacía.
- **RN-PROF-006:** El avatar es una selección fija de íconos predefinidos. Si no se selecciona ninguno, se asigna ⭐ por defecto.
- **RN-PROF-007:** No se permite registrar un valor de avatar que no esté dentro de las opciones definidas.

---

## Criterios de Terminación

- [ ] Todos los campos del perfil son visibles y editables
- [ ] El avatar se muestra como selección de íconos y se guarda correctamente
- [ ] El avatar por defecto es ⭐ si el usuario no selecciona ninguno
- [ ] El género por defecto es *Prefiero no decirlo* si el usuario no selecciona ninguno
- [ ] Las validaciones de cada campo muestran mensajes claros al usuario
- [ ] El perfil no expone ni permite modificar nombre, correo ni contraseña
- [ ] Pruebas unitarias completadas (cobertura mayor al 80%)
- [ ] Pruebas de aceptación aprobadas para cada criterio
- [ ] Documentación del módulo de perfil actualizada
