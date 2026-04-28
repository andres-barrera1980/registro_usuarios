# Registro de Usuarios

Aplicación de consola en Java 21 siguiendo los principios de **Clean Architecture**.

## Arquitectura

El proyecto está organizado en las siguientes capas:

- **Domain**: Contiene las entidades de negocio y las interfaces de los repositorios. Es el núcleo de la aplicación y no tiene dependencias externas.
- **Application**: Contiene los casos de uso (lógica de aplicación) y los puertos (interfaces de entrada y salida).
- **Infrastructure**: Implementaciones de los puertos, adaptadores de persistencia, adaptadores de entrada (consola) y configuración.

## Requisitos

- Java 21
- Maven 3.x

## Ejecución

Para compilar el proyecto:
```bash
mvn clean compile
```

Para ejecutar los tests:
```bash
mvn test
```

## Autor
Andrés Barrera
