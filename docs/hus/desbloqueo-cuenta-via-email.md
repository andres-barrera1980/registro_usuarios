# HU-AUTH-005 — Desbloqueo de cuenta bloqueada vía email

## Historia de Usuario

```
Como buyer cuya cuenta ha sido bloqueada por intentos fallidos,
Quiero recibir un correo electrónico con un enlace para desbloquear
mi cuenta de forma segura,
Para recuperar el acceso a la plataforma sin tener que esperar
el tiempo de desbloqueo automático.
```

> **Valor de negocio:** Un usuario bloqueado sin salida rápida abandona la plataforma.
> Ofrecer un desbloqueo inmediato vía email reduce la frustración, retiene al comprador
> y demuestra que la plataforma cuida la experiencia de sus usuarios.

---

## Validación INVEST

| Principio   | Estado | Justificación                                                                          |
|-------------|:------:|----------------------------------------------------------------------------------------|
| Independent | ✅     | El desbloqueo es una capacidad separada del bloqueo (HU-AUTH-002)                     |
| Negotiable  | ✅     | El tiempo de validez del enlace y el canal (email) son negociables con el negocio      |
| Valuable    | ✅     | Permite al buyer recuperar acceso sin esperar, mejorando retención                     |
| Estimable   | ✅     | Alcance claro y acotado → 3–5 story points                                             |
| Small       | ✅     | Completable en un sprint de forma independiente                                        |
| Testable    | ✅     | El enlace, su expiración y los estados resultantes son verificables en cada escenario  |

---

## Criterios de Aceptación

### AC-001 — Envío del correo de desbloqueo al quedar bloqueado
```gherkin
Given que mi cuenta acaba de ser bloqueada por superar el límite de intentos fallidos,
When el sistema aplica el bloqueo,
Then recibo un correo electrónico en mi dirección registrada
  And el correo contiene un enlace seguro para desbloquear mi cuenta
  And el enlace tiene una vigencia limitada claramente indicada en el correo.
```

### AC-002 — Desbloqueo exitoso mediante el enlace
```gherkin
Given que recibí el correo de desbloqueo y el enlace aún está vigente,
When hago clic en el enlace de desbloqueo,
Then el sistema reactiva mi cuenta
  And el contador de intentos fallidos se reinicia a cero
  And soy informado de que mi cuenta fue desbloqueada exitosamente
  And puedo iniciar sesión normalmente.
```

### AC-003 — Enlace de desbloqueo expirado
```gherkin
Given que recibí el correo de desbloqueo
  And el enlace ya superó su tiempo de vigencia,
When hago clic en el enlace,
Then el sistema me informa que el enlace ha expirado
  And me ofrece la opción de solicitar un nuevo enlace de desbloqueo.
```

### AC-004 — Enlace de desbloqueo de un solo uso
```gherkin
Given que ya usé el enlace de desbloqueo exitosamente,
When intento usar el mismo enlace nuevamente,
Then el sistema lo rechaza
  And me informa que el enlace ya fue utilizado.
```

### AC-005 — Solicitud de reenvío del correo de desbloqueo
```gherkin
Given que mi cuenta está bloqueada
  And no recibí el correo o el enlace expiró,
When solicito que se reenvíe el correo de desbloqueo,
Then el sistema genera un nuevo enlace y lo envía a mi correo registrado
  And el enlace anterior queda invalidado automáticamente.
```

### AC-006 — No se puede desbloquear una cuenta suspendida por administrador
```gherkin
Given que mi cuenta está en estado SUSPENDED por acción administrativa
  (no por bloqueo automático por intentos),
When intento usar un enlace de desbloqueo o solicitar uno,
Then el sistema rechaza la acción
  And me informa que mi cuenta está suspendida y debo contactar soporte.
```

---

## Reglas de Negocio

| ID          | Regla                                                                                                          |
|-------------|----------------------------------------------------------------------------------------------------------------|
| RN-AUTH-016 | El enlace de desbloqueo es válido por **1 hora** desde su generación (configurable)                           |
| RN-AUTH-017 | El enlace de desbloqueo es de **un solo uso**; se invalida tras el primer clic exitoso                        |
| RN-AUTH-018 | Solo se puede tener **un enlace de desbloqueo activo** a la vez; al solicitar reenvío, el anterior se invalida |
| RN-AUTH-019 | El desbloqueo vía email solo aplica a cuentas en estado **LOCKED** (bloqueo automático por intentos)          |
| RN-AUTH-020 | Las cuentas en estado **SUSPENDED** (por administrador) no pueden desbloquearse vía email                     |
| RN-AUTH-021 | El evento `AccountUnlocked { userId, unlockedAt, method: "EMAIL_LINK" }` se registra para auditoría           |

---

## Definition of Done

### Funcional
- [ ] El correo de desbloqueo se envía automáticamente al momento del bloqueo
- [ ] El enlace desbloquea la cuenta correctamente si está vigente
- [ ] El enlace expirado informa al usuario y ofrece reenvío
- [ ] El enlace usado una vez no puede reutilizarse
- [ ] El reenvío invalida el enlace anterior
- [ ] Las cuentas SUSPENDED no pueden desbloquearse por este flujo

### Calidad
- [ ] Tests unitarios con cobertura ≥ 80% en lógica de generación y validación del enlace
- [ ] Test de expiración: enlace rechazado tras el tiempo configurado
- [ ] Test de un solo uso: segundo intento con el mismo enlace es rechazado
- [ ] Tests de aceptación pasan para AC-001 a AC-006

### Técnico *(interno del equipo)*
- [ ] Endpoint `GET /api/auth/unlock?token=<token>` documentado con OpenAPI 3.0
- [ ] Endpoint `POST /api/auth/resend-unlock` documentado con OpenAPI 3.0
- [ ] Token de desbloqueo almacenado con hash (no en texto plano)
- [ ] Envío de email procesado de forma asíncrona (no bloquea la respuesta HTTP)
- [ ] Respuestas de error con estructura consistente (Problem Detail)
- [ ] Evento `AccountUnlocked` emitido al sistema de auditoría

---

## Dependencias

| ID          | Historia / Componente                             | Tipo       |
|-------------|---------------------------------------------------|------------|
| HU-AUTH-001 | Login básico implementado                         | Bloqueante |
| HU-AUTH-002 | Bloqueo por intentos fallidos implementado        | Bloqueante |
| INFRA       | Servicio de envío de emails configurado (SMTP)    | Bloqueante |

---

## Historias Relacionadas

| Historia    | Descripción                                          |
|-------------|------------------------------------------------------|
| HU-AUTH-002 | Bloqueo de cuenta (genera el estado LOCKED)          |
| HU-AUTH-001 | Login básico (punto de entrada tras desbloqueo)      |

---

## Flujo de Usuario

```
[Cuenta bloqueada]
      │
      ├──▶ Sistema envía email con enlace de desbloqueo
      │
      └──▶ Usuario recibe email
                │
                ├── Clic en enlace vigente ──▶ Cuenta desbloqueada ──▶ Login normal ✅
                │
                ├── Clic en enlace expirado ──▶ Mensaje de error + opción de reenvío
                │         │
                │         └── Solicita reenvío ──▶ Nuevo enlace enviado (anterior invalidado)
                │
                └── Clic en enlace ya usado ──▶ Mensaje: "Enlace ya utilizado" ❌
```

---

## Notas de Implementación *(para el equipo técnico)*

> Estos detalles **no son criterios de aceptación negociables** con el cliente.

- Token de desbloqueo: UUID v4, almacenado como hash SHA-256 en tabla `unlock_tokens`
- Estructura de `UnlockToken`: `{ tokenHash, userId, createdAt, expiresAt, usedAt, isRevoked }`
- Endpoint de desbloqueo: `GET /api/auth/unlock?token=<raw-uuid>`
  - Valida existencia, expiración (`expiresAt > now`), y que `usedAt IS NULL`
  - Transición de estado: `LOCKED → ACTIVE`; establece `usedAt = now`
- Endpoint de reenvío: `POST /api/auth/resend-unlock` con body `{ email }`
  - Invalida tokens previos (`isRevoked = true`) antes de generar el nuevo
  - Rate limiting: máx. 3 reenvíos por hora por usuario (previene abuso)
- Email enviado de forma asíncrona vía Outbox Pattern
- Template del email debe incluir: nombre del usuario, hora del bloqueo, botón CTA, tiempo de vigencia del enlace
- Estado de cuenta: `LOCKED` (bloqueo automático) distinto de `SUSPENDED` (acción administrativa)

---

*Estimación: 3–5 story points | Sprint: AUTH-2 | Prioridad: Alta*
