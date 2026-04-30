# HU-AUTH-001 — Inicio de sesión como buyer

## Historia de Usuario

```
Como buyer registrado en OpenLib,
Quiero iniciar sesión con mi email y contraseña,
Para obtener acceso autenticado a la plataforma y usar
sus funcionalidades como comprador.
```

> **Valor de negocio:** Sin autenticación, el buyer no puede comprar,
> gestionar su carrito ni acceder a su historial de pedidos.

---

## Validación INVEST

| Principio       | Estado | Justificación                                                       |
|-----------------|:------:|---------------------------------------------------------------------|
| Independent     | ✅     | No depende de bloqueo, sesión extendida ni remember-me              |
| Negotiable      | ✅     | Los ACs describen comportamiento, no tecnología                     |
| Valuable        | ✅     | Habilita el acceso a todas las funcionalidades del buyer            |
| Estimable       | ✅     | Alcance acotado → 5–8 story points                                  |
| Small           | ✅     | Completable en un sprint                                            |
| Testable        | ✅     | Cada AC tiene Given/When/Then verificable                           |

---

## Criterios de Aceptación

### AC-001 — Login exitoso
```gherkin
Given que soy un buyer con cuenta ACTIVA y credenciales correctas,
When ingreso mi email y contraseña e intento iniciar sesión,
Then el sistema me autentica exitosamente
  And recibo un token de acceso con su fecha de expiración
  And quedo habilitado para usar las funcionalidades de la plataforma.
```

### AC-002 — Credenciales incorrectas
```gherkin
Given que ingreso una contraseña que no corresponde a mi cuenta,
When intento iniciar sesión,
Then el sistema rechaza el intento con un mensaje genérico
  ("Email o contraseña incorrectos")
  And no revela cuál de los dos campos es incorrecto
  And el intento fallido queda registrado en el sistema.
```

### AC-003 — Cuenta sin verificar
```gherkin
Given que tengo una cuenta registrada pero no he verificado mi email,
When intento iniciar sesión con credenciales correctas,
Then el sistema me informa que debo verificar mi email antes de continuar
  And me ofrece la opción de reenviar el correo de verificación.
```

### AC-004 — Cuenta suspendida
```gherkin
Given que mi cuenta está suspendida,
When intento iniciar sesión,
Then el sistema me niega el acceso
  And me informa que mi cuenta se encuentra suspendida.
```

### AC-005 — Token inválido o expirado
```gherkin
Given que mi token de sesión ha expirado o es inválido,
When intento acceder a una funcionalidad que requiere autenticación,
Then el sistema rechaza mi solicitud
  And me solicita que inicie sesión nuevamente.
```

---

## Reglas de Negocio

| ID          | Regla                                                                                                          |
|-------------|----------------------------------------------------------------------------------------------------------------|
| RN-AUTH-001 | El mensaje de error en credenciales incorrectas nunca debe indicar cuál campo falló (previene enumeración de usuarios) |
| RN-AUTH-002 | El token de sesión expira por inactividad (tiempo configurable, default 30 min)                               |
| RN-AUTH-003 | El sistema registra cada intento de login (exitoso o fallido) con fines de auditoría                         |

---

## Definition of Done

### Funcional
- [ ] El buyer puede iniciar sesión con credenciales válidas y recibir un token
- [ ] Los estados ACTIVE, UNVERIFIED y SUSPENDED responden correctamente
- [ ] Los mensajes de error no revelan información sensible
- [ ] El token expirado es rechazado en endpoints protegidos

### Calidad
- [ ] Tests unitarios con cobertura ≥ 80% en lógica de autenticación
- [ ] Tests de integración para el endpoint de login
- [ ] Tests de aceptación pasan para AC-001 a AC-005

### Técnico *(interno del equipo)*
- [ ] Endpoint `POST /api/auth/login` documentado con OpenAPI 3.0
- [ ] Respuestas de error con estructura consistente (Problem Detail)
- [ ] Integración con sistema de auditoría de intentos (log de eventos)

### Operacional
- [ ] Health check del servicio de autenticación disponible

---

## Dependencias

| ID      | Historia / Componente                                | Tipo      |
|---------|------------------------------------------------------|-----------|
| TAB-36  | HU-REG-001 — Registro de buyer                       | Bloqueante |
| INFRA   | Base de datos de usuarios disponible                 | Bloqueante |

---

## Historias Relacionadas

| Historia      | Descripción                                    |
|---------------|------------------------------------------------|
| HU-AUTH-002   | Bloqueo por intentos fallidos                  |
| HU-AUTH-003   | Gestión de sesión activa (token sliding)       |
| HU-AUTH-004   | Sesión extendida ("Recuérdame")                |

---

## Notas de Implementación *(para el equipo técnico)*

> Estos detalles **no son criterios de aceptación negociables** con el cliente.
> Son decisiones de arquitectura interna del equipo.

- Autenticación de contraseña: BCryptPasswordEncoder
- Token de sesión: UUID v4 (Spring Session)
- Formato de error: RFC 7807 Problem Detail
- Almacenamiento de sesión: Spring Session JDBC + PostgreSQL
- Auditoría: Evento `LoginAttempted { userId, success, timestamp, ipAddress }`

---

*Estimación: 5–8 story points | Sprint: AUTH-1 | Prioridad: Alta*
