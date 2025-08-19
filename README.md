# ForoHub

ForoHub es una aplicación desarrollada con **Spring Boot** que implementa un sistema de foros con autenticación de usuarios, gestión de tópicos y seguridad basada en JWT.  
Este proyecto forma parte del aprendizaje en el programa **Oracle Next Education (ONE)**.

## Características

- Autenticación y autorización con **JWT**.
- Creación, edición y eliminación de **tópicos**.
- Gestión de usuarios con roles **ADMIN** y **USER**.
- Migraciones de base de datos con **Flyway**.
- Arquitectura basada en **Spring Boot + Spring Security + JPA/Hibernate**.
- API REST lista para integrarse con frontends modernos.

## Tecnologías

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA (Hibernate)
- Flyway
- Maven
- JWT (JSON Web Tokens)
- H2 / MySQL (configurable en `application.properties`)

## Instalación y ejecución

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/hugocrocco/forohub.git
   cd forohub

Ejecutar con Maven (sin tests):
./mvnw spring-boot:run -DskipTests

La aplicación estará disponible en http://localhost:8080

Autenticación

{
  "email": "admin@mail.com",
  "password": "admin123"
}

Respuesta: retorna un JWT.


Tópicos
	•	GET /topicos → Lista todos los tópicos.
	•	POST /topicos → Crea un nuevo tópico.
	•	PUT /topicos/{id} → Actualiza un tópico existente.
	•	DELETE /topicos/{id} → Elimina un tópico.


Desarrollado por Hugo Crocco para el curso Alura Latam 




   
