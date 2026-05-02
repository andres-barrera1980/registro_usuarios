# Plan de Implementación: Persistencia en JSON Local

Este documento define la lista de tareas para reemplazar la persistencia en memoria por una persistencia en un archivo local `usuarios.json`, implementando el `UsuarioGateway` bajo el patrón de Arquitectura Limpia (Capa de Infraestructura / Adaptadores de Salida).

## Diseño de la Solución

- **Librería**: Se utilizará **Jackson** (`jackson-databind` y `jackson-datatype-jsr310` para manejar fechas `LocalDateTime`).
- **Archivo de datos**: Un archivo local llamado `usuarios.json` en la raíz del proyecto.
- **Adaptador**: Se creará la clase `JsonUsuarioGateway` en el paquete `infrastructure.adapter.out.persistence` que implemente la interfaz `UsuarioGateway`.

## Lista de Tareas (Implementación Persistencia)

- [x] **1. Configurar Dependencias**:
  - Agregar `jackson-databind` y `jackson-datatype-jsr310` al `pom.xml`.
- [x] **2. Crear Adaptador de Persistencia (`JsonUsuarioGateway`)**:
  - Crear la clase en `co.edu.javeriana.registro.infrastructure.adapter.out.persistence`.
  - Implementar constructor que reciba la ruta del archivo y asegure que el archivo exista (o lo cree vacío si no).
- [x] **3. Implementar Operaciones del Gateway**:
  - Implementar método interno genérico para leer el archivo JSON y convertirlo a una lista/mapa de usuarios.
  - Implementar método interno genérico para escribir la lista/mapa de usuarios al archivo JSON.
  - Implementar `guardar(Usuario)`: Leer, actualizar/agregar, escribir.
  - Implementar `buscarPorEmail(String)`: Leer y buscar.
  - Implementar `buscarCuentasInactivasExpiradas(LocalDateTime)`: Leer y filtrar.
  - Implementar `eliminar(Usuario)`: Leer, remover, escribir.
- [x] **4. Escribir Pruebas Unitarias/Integración**:
  - Crear `JsonUsuarioGatewayTest` utilizando un archivo temporal (TempDir de JUnit 5) para probar lectura y escritura correcta.
- [x] **5. Conectar Adaptador al Entry Point**:
  - Modificar `Main.java` para instanciar `JsonUsuarioGateway("usuarios.json")` en lugar del gateway en memoria anónimo.
