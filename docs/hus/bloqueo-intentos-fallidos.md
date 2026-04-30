# HU-AUTH-002 — Bloqueo de cuenta por intentos fallidos

## Historia de Usuario

```
Como buyer de OpenLib,
Quiero que mi cuenta quede protegida automáticamente
tras múltiples intentos de acceso fallidos,
Para que nadie pueda acceder a mi cuenta por fuerza bruta
sin mi conocimiento.
```

> **Valor de negocio:** Protege las cuentas y la confianza del usuario en la plataforma.
> Un buyer que siente que su cuenta es segura es un buyer que compra con confianza.

---

## Validación INVEST

| Principio   | Estado | Justificación                                                                 |
|-------------|:------:|-------------------------------------------------------------------------------|
| Independent | ✅     | Depende de que el login exista (HU-AUTH-001), pero es una capacidad separada  |
| Negotiable  | ✅     | Número de intentos y tiempo de bloqueo son negociables con el negocio         |
| Valuable    | ✅     | Protege activos del usuario y reduce riesgo de fraude en la plataforma        |
| Estimable   | ✅     | Alcance claro → 3–5 story points                                              |
| Small       | ✅     | Completable en un sprint, independiente del flujo principal                   |
| Testable    | ✅     | Cada AC tiene condición de bloqueo verificable con escenarios concretos       |

---

## Criterios de Aceptación

### AC-001 — Bloqueo automático tras intentos fallidos consecutivos
```gherkin
Given que he fallado el inicio de sesión 5 veces consecutivas,
When intento iniciar sesión por sexta vez,
Then el sistema bloquea temporalmente mi cuenta
  And me informa que mi cuenta ha sido bloqueada por actividad sospechosa
  And me indica cuánto tiempo debo esperar antes de volver a intentarlo.
```

### AC-002 — Notificación al usuario sobre el bloqueo
```gherkin
Given que mi cuenta acaba de ser bloqueada por intentos fallidos,
When el sistema aplica el bloqueo,
Then recibo un correo electrónico notificándome del bloqueo
  And el correo indica la hora del bloqueo y los pasos para recuperar acceso.
```

### AC-003 — Desbloqueo automático por tiempo
```gherkin
Given que mi cuenta fue bloqueada por intentos fallidos
  And ha transcurrido el tiempo de bloqueo configurado,
When intento iniciar sesión con mis credenciales correctas,
Then el sistema me permite acceder normalmente
  And el contador de intentos fallidos se reinicia.
```

### AC-004 — Intento durante período de bloqueo
```gherkin
Given que mi cuenta está actualmente bloqueada,
When intento iniciar sesión (con credenciales correctas o incorrectas),
Then el sistema me rechaza el acceso
  And me informa que la cuenta está bloqueada y el tiempo restante de bloqueo.
```

### AC-005 — Reinicio del contador en login exitoso
```gherkin
Given que he tenido intentos fallidos pero no he alcanzado el límite,
When inicio sesión exitosamente,
Then el contador de intentos fallidos se reinicia a cero.
```

---

## Reglas de Negocio

| ID          | Regla                                                                                                   |
|-------------|---------------------------------------------------------------------------------------------------------|
| RN-AUTH-006 | El bloqueo se activa al superar **5 intentos fallidos consecutivos**                                    |
| RN-AUTH-007 | La duración del bloqueo temporal es de **30 minutos** (configurable)                                   |
| RN-AUTH-008 | El contador de intentos fallidos solo se resetea con un login exitoso o tras el desbloqueo              |
| RN-AUTH-009 | Se notifica al usuario por email cuando su cuenta es bloqueada                                          |
| RN-AUTH-010 | El sistema registra el evento de bloqueo con timestamp, IP y userId para auditoría                      |

---

## Definition of Done

### Funcional
- [ ] La cuenta se bloquea tras 5 intentos fallidos consecutivos
- [ ] El sistema informa al usuario el tiempo restante de bloqueo
- [ ] El desbloqueo automático ocurre tras el tiempo configurado
- [ ] El contador se reinicia correctamente tras login exitoso
- [ ] El usuario recibe notificación por email al ser bloqueado

### Calidad
- [ ] Tests unitarios con cobertura ≥ 80% en lógica de bloqueo
- [ ] Tests de integración: flujo completo de bloqueo y desbloqueo
- [ ] Tests de aceptación pasan para AC-001 a AC-005

### Técnico *(interno del equipo)*
- [ ] Evento `AccountLocked { userId, lockedAt, unlockAt, ipAddress }` emitido
- [ ] Rate limiting aplicado en endpoint de login
- [ ] Respuestas de error con estructura consistente (Problem Detail)
- [ ] Endpoint documentado con OpenAPI 3.0 (respuesta HTTP 423)

---

## Dependencias

| ID          | Historia / Componente                    | Tipo       |
|-------------|------------------------------------------|------------|
| HU-AUTH-001 | Login básico implementado                | Bloqueante |
| RN-AUTH-006 | Regla de negocio: umbral de intentos     | Referencia |

---

## Historias Relacionadas

| Historia    | Descripción                            |
|-------------|----------------------------------------|
| HU-AUTH-001 | Login básico (prerequisito)            |
| HU-AUTH-003 | Gestión de sesión activa               |

---

## Notas de Implementación *(para el equipo técnico)*

> Estos detalles **no son criterios de aceptación negociables** con el cliente.

- Implementar `@Version` en entidad `User` para optimistic locking (RN-AUTH-019)
- Rate limiting: Bucket4j (5 intentos / ventana configurable)
- Transición de estado: `ACTIVE → SUSPENDED` tras bloqueo; `unlockAt = now + 30min`
- Evento de dominio: `AccountLocked` publicado vía Outbox Pattern
- Notificación por email procesada de forma asíncrona (no bloquea la respuesta HTTP)
- Estado de bloqueo: HTTP 423 con `Retry-After` header

---

*Estimación: 3–5 story points | Sprint: AUTH-1 | Prioridad: Alta*
