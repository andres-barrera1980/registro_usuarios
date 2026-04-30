# Diseño Técnico: Gestión de Perfil de Usuario

Este documento describe el plan de implementación para las funcionalidades asociadas a la gestión del Perfil de Usuario (Crear, Actualizar y Visualizar), cumpliendo con los Criterios de Aceptación y Reglas de Negocio de la Historia de Usuario correspondiente. El diseño se basa en **Clean Architecture**, **Domain-Driven Design (DDD)** y **SOLID**.

## 1. Análisis y Modelado de Dominio

De acuerdo con las reglas de negocio, el perfil maneja datos complementarios y su edición no afecta al nombre, correo ni estado de la cuenta. Para mantener la integridad, encapsularemos esto en un **Value Object** llamado `Perfil` que pertenece al Aggregate Root `Usuario`.

### 1.1 Enumeraciones necesarias (Enums)
*   **`Genero`**: Valores `MASCULINO`, `FEMENINO`, `PREFIERO_NO_DECIRLO`.
*   **`Avatar`**: Valores `STAR`, `MOON`, `SUN`, `HEART`, `DIAMOND`, `BOLT`, `LEAF`, `CROWN`, `FLAME`, `GHOST`. Cada uno puede almacenar internamente su representación visual (ej. "⭐").

### 1.2 `Perfil` (Value Object)
*   **Atributos**:
    *   `telefono` (String)
    *   `fechaNacimiento` (LocalDate) - Utilizado para validar y derivar la edad.
    *   `direccion` (String)
    *   `genero` (Genero)
    *   `avatar` (Avatar)
*   **Reglas de Negocio en el Constructor/Factory**:
    *   **RN-PROF-002**: `telefono` debe contener solo dígitos y tener mínimo 7 caracteres.
    *   **RN-PROF-003**: `fechaNacimiento` debe corresponder a una edad mínima de 14 años respecto a la fecha actual.
    *   **RN-PROF-005**: `direccion` es obligatoria y no puede estar vacía.
    *   **RN-PROF-004 & RN-PROF-006**: Si `genero` o `avatar` no se especifican (nulos), el constructor les asignará por defecto `PREFIERO_NO_DECIRLO` y `STAR` respectivamente.

### 1.3 `Usuario` (Aggregate Root)
*   **Nuevo Atributo**: `private Perfil perfil;`
*   **Nuevos Comportamientos**: 
    *   `public void crearPerfil(Perfil perfil)`: Asigna un perfil por primera vez.
    *   `public void actualizarPerfil(Perfil perfil)`: Modifica el perfil existente (reemplaza el Value Object).
    *   `public Perfil getPerfil()`: Retorna el perfil.
*   **Reglas de Negocio**: 
    *   **RN-PROF-001**: Queda garantizado, ya que nombre y correo viven en `Usuario` y no mutan a través del objeto `Perfil`.

## 2. Capa de Aplicación (Use Cases / Interactors)

Se deben crear 3 interactores distintos para cumplir con las 3 funcionalidades principales:

1.  **`CrearPerfilUsuarioInteractor`**:
    *   *Flujo*: Recibe el email del usuario y los datos del perfil (DTO). Recupera el usuario usando `UsuarioGateway`. Si ya tiene perfil, podría lanzar un error o advertir. Instancia el `Perfil` (se validan las reglas allí), llama a `usuario.crearPerfil()`, y guarda usando el gateway.
2.  **`ActualizarPerfilUsuarioInteractor`**:
    *   *Flujo*: Recibe el email y los nuevos datos del perfil. Recupera el usuario, verifica que ya exista un perfil, crea una nueva instancia de `Perfil` con los datos actualizados, llama a `usuario.actualizarPerfil()`, y guarda.
3.  **`VisualizarPerfilUsuarioInteractor`**:
    *   *Flujo*: Recibe el email del usuario. Retorna un DTO consolidado que incluye la información base (`nombre`, `email`) extraída de `Usuario` y los datos adicionales (`avatar`, `telefono`, edad calculada) extraídos de `Perfil`.

## 3. Capa de Infraestructura (Gateways y Persistencia)

*   **Base de Datos Relacional (JPA/SQLite)**:
    *   Crear una clase `@Embeddable` llamada `PerfilJpaEmbeddable` con las columnas: `perfil_telefono`, `perfil_fecha_nacimiento`, `perfil_direccion`, `perfil_genero`, `perfil_avatar`.
    *   Actualizar `UsuarioJpaEntity` agregando este campo embebido (`@Embedded`) con anotaciones `@Enumerated(EnumType.STRING)` para los enums.
    *   Actualizar los mappers (`toDomain` y `toEntity`) en `SqliteUsuarioGateway` para conversiones bidireccionales de `Perfil`.
*   **Base de Datos Documental (JSON)**:
    *   Verificar que `JsonUsuarioGateway` mapee correctamente los enums y el objeto anidado `perfil`. Esto suele ser automático en Jackson mediante `jackson-datatype-jsr310` para las fechas.

## 4. Adaptadores de Entrada (UI / Consola)

*   **`MenuConsola`**: Se incorporarán las siguientes opciones al flujo del sistema:
    *   **Ver mi perfil**: Llama a `VisualizarPerfilUsuarioInteractor` e imprime en consola los datos del usuario, el avatar (emoji) y la información extendida.
    *   **Crear mi perfil**: Inicia un asistente que pide el teléfono, fecha nacimiento (YYYY-MM-DD), dirección, y ofrece un menú con opciones numéricas para seleccionar género y avatar. Llama a `CrearPerfilUsuarioInteractor`.
    *   **Editar mi perfil**: Reutiliza parte del flujo de creación para solicitar nuevos datos. Llama a `ActualizarPerfilUsuarioInteractor`.

## 5. Plan de Pruebas Unitarias e Integración (TDD)

*   **Dominio**:
    *   Crear `PerfilTest`: Pruebas intensivas sobre RN-PROF-002 (teléfono regex o numérico + longitud), RN-PROF-003 (test paramétrico con fechas para <=13, 14, >=15 años), y asignación de valores por defecto (STAR, PREFIERO_NO_DECIRLO).
*   **Aplicación**:
    *   Tests para `CrearPerfilUsuarioInteractorTest`, `ActualizarPerfilUsuarioInteractorTest` y `VisualizarPerfilUsuarioInteractorTest` utilizando Mockito para aislar el caso de uso y simular respuestas del Gateway.
*   **Infraestructura**:
    *   Pruebas de persistencia integradas: Modificar `SqliteUsuarioGatewayTest` guardando un usuario con perfil completo, para asegurar que los Enums y `LocalDate` se insertan/consultan limpiamente en la DB.

---

## 📋 Tareas de Implementación (Checklist)

- [ ] 1. Crear Enum `Genero` en `domain/model`.
- [ ] 2. Crear Enum `Avatar` en `domain/model` (con la representación de emojis).
- [ ] 3. Crear el Value Object `Perfil` en `domain/model` con las validaciones del negocio (RN-PROF-002 a 007).
- [ ] 4. Actualizar entidad `Usuario` para soportar `perfil` (`crearPerfil`, `actualizarPerfil`, `getPerfil`).
- [ ] 5. Escribir pruebas unitarias del dominio para `Perfil` y la actualización de `Usuario`.
- [ ] 6. Implementar `CrearPerfilUsuarioInteractor` y su test.
- [ ] 7. Implementar `ActualizarPerfilUsuarioInteractor` y su test.
- [ ] 8. Implementar `VisualizarPerfilUsuarioInteractor` y su test.
- [ ] 9. Crear el `@Embeddable` `PerfilJpaEmbeddable` en `infrastructure`.
- [ ] 10. Actualizar `UsuarioJpaEntity` y mappers en `SqliteUsuarioGateway`.
- [ ] 11. Ejecutar tests de Integración para SQLite y JSON verificando el guardado/recuperado del perfil.
- [ ] 12. Actualizar `MenuConsola` implementando flujos de visualización, creación y edición interactiva del perfil.
