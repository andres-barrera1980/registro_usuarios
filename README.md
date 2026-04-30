# Registro de Usuarios

Aplicación de consola en Java 21 construida aplicando rigurosamente **Clean Architecture**, **Domain-Driven Design (DDD)**, **TDD**, y los **Principios SOLID**.

El sistema permite gestionar el registro de usuarios, la emisión de códigos de validación y la limpieza automática de cuentas inactivas; todo completamente desacoplado y soportando múltiples mecanismos de persistencia.

---

## 🏛 Arquitectura y Diseño

El proyecto sigue estrictamente el patrón de **Arquitectura Limpia**, dividiendo el código en anillos concéntricos con la **Regla de Dependencia** fluyendo siempre hacia el núcleo (Dominio).

### Capas de la Arquitectura

1. **`domain` (Núcleo / Entidades y Casos de Uso del Negocio)**:
   - Contiene las reglas de negocio puras.
   - Implementado mediante **DDD**:
     - *Aggregate Root*: `Usuario`.
     - *Value Object*: `CodigoValidacion`.
     - *Excepciones de Dominio*: Centralizadas para evitar fugas de lógica (`CodigoExpiradoException`, `DomainException`, etc.).
   - No tiene ninguna dependencia externa (ni de frameworks, bases de datos o UI).

2. **`application` (Casos de Uso de la Aplicación)**:
   - Orquesta el flujo de datos hacia y desde el dominio.
   - **`interactor`**: Contiene los casos de uso específicos del sistema (`ActivarCuentaUsuarioInteractor`, `EmitirCodigoValidacionInteractor`, etc.).
   - **`gateway`**: Define los puertos de salida o interfaces (`UsuarioGateway`, `EmailGateway`) requeridos por los interactores para persistencia y servicios externos aplicando el principio de *Inversión de Dependencias (DIP)*.

3. **`interfaces` (Adaptadores de Entrada)**:
   - Contiene la interacción con el usuario.
   - Implementa `MenuConsola` que consume a los `interactors` de la capa de aplicación. 

4. **`infrastructure` (Adaptadores de Salida)**:
   - Implementa las interfaces de la capa de aplicación (`Gateways`) hacia tecnologías externas.
   - **Persistencia Múltiple**:
     - `JsonUsuarioGateway`: Adaptador basado en archivos locales `.json` utilizando *Jackson*.
     - `SqliteUsuarioGateway`: Adaptador basado en *SQLite* a través de *JPA/Hibernate*.
   - **Independencia de Framework**: La persistencia en JPA utiliza su propia entidad `UsuarioJpaEntity` que se mapea desde/hacia la entidad de dominio `Usuario`, asegurando que el Dominio nunca dependa de anotaciones de Hibernate.

---

## 📂 Estructura de Directorios

```text
src/main/java/co/edu/javeriana/registro/
├── domain/                               # Capa Core (Reglas de Negocio)
│   ├── model/                            # Agregados y Value Objects (Usuario, CodigoValidacion)
│   │   └── PasswordHasher.java           # Puerto para abstracción de seguridad
│   └── exception/                        # Excepciones propias del dominio
├── application/                          # Capa de Orquestación
│   ├── interactor/                       # Casos de Uso (Emisión, Activación, Limpieza)
│   └── gateway/                          # Interfaces (Puertos) de salida
├── interfaces/                           # Capa de Interacción de Usuario
│   └── MenuConsola.java                  # Menú interactivo CLI
├── infrastructure/                       # Capa de Infraestructura Técnica
│   └── adapter/out/                      # Adaptadores Externos
│       ├── BcryptPasswordHasher.java     # Implementación de seguridad usando BCrypt
│       └── persistence/                  # Adaptadores de Persistencia
│           ├── JsonUsuarioGateway.java   # Implementación JSON
│           └── jpa/                      # Implementación Relacional (JPA/SQLite)
│               ├── SqliteUsuarioGateway.java # Adaptador JPA
│               └── UsuarioJpaEntity.java # Entidad de persistencia
└── Main.java                             # Punto de entrada / Inyección de Dependencias
```

---

## 🛠 Requisitos y Tecnologías

- **Lenguaje**: Java 21
- **Construcción**: Maven 3.x
- **Seguridad**: at.favre.lib:bcrypt (BCrypt v0.10.2)
- **Testing**: JUnit 5, Mockito (100% de cobertura en el Dominio e Interactors mediante TDD).
- **Persistencia JSON**: Jackson Databind, Jackson JSR310.
- **Persistencia Relacional**: Hibernate Core 6.4, SQLite JDBC.

---

## 🚀 Ejecución y Uso

1. **Compilar el proyecto y descargar dependencias**:
   ```bash
   mvn clean compile
   ```

2. **Ejecutar las pruebas unitarias y de integración**:
   ```bash
   mvn test
   ```

3. **Iniciar la aplicación interactiva de consola**:
   ```bash
   mvn exec:java -Dexec.mainClass="co.edu.javeriana.registro.Main"
   ```

Al iniciar la aplicación, el sistema cargará el Menú de Consola configurado actualmente para persistir en SQLite (`usuarios.db`). El esquema de la base de datos se genera y actualiza de manera automática.

## 👤 Autor
Andrés Barrera
