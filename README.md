# FindMyService

A Spring Boot backend for a service marketplace where users can browse providers, place orders, leave feedback, and manage accounts with JWT-based authentication. Includes Swagger/OpenAPI docs and role-based access control.

## Features
- User registration/login with JWT
- Role-based access (`USER`, `PROVIDER`, `ADMIN`)
- Service catalog and provider management
- Orders lifecycle with status updates
- Feedback and ratings
- Swagger UI for API exploration

## Tech Stack
- Java 17+ (OpenJDK)
- Spring Boot
- Spring Security (JWT)
- Maven (with `mvnw` wrapper)
- Swagger/OpenAPI

## Project Structure
- Core app: [src/main/java/com/FindMyService](src/main/java/com/FindMyService)
  - Config: [config](src/main/java/com/FindMyService/config)
  - Controllers: [controller](src/main/java/com/FindMyService/controller)
  - Models/DTOs/Enums: [model](src/main/java/com/FindMyService/model)
  - Repositories: [repository](src/main/java/com/FindMyService/repository)
  - Security (JWT): [security](src/main/java/com/FindMyService/security)
  - Services: [service](src/main/java/com/FindMyService/service)
  - Utilities: [utils](src/main/java/com/FindMyService/utils)
- Tests: [src/test/java/com/FindMyService](src/test/java/com/FindMyService)
- Entry point: [FindMyServiceApplication.java](src/main/java/com/FindMyService/FindMyServiceApplication.java)

## Getting Started
### Prerequisites
- macOS/Linux/Windows
- Java 17+ installed (verify with `java -version`)

### Setup
- Clone/open the project, then use the Maven wrapper:

```bash
./mvnw -v
./mvnw clean install
```

## Run
Start the application:

```bash
./mvnw spring-boot:run
```

By default the server runs on `http://localhost:8080`.

## API Docs (Swagger)
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

See configuration in [SwaggerConfig.java](src/main/java/com/FindMyService/config/SwaggerConfig.java).

## Authentication
- Login endpoint issues JWT: see [AuthController.java](src/main/java/com/FindMyService/controller/AuthController.java) and [AuthService.java](src/main/java/com/FindMyService/service/AuthService.java).
- JWT filter: [JwtAuthFilter.java](src/main/java/com/FindMyService/security/JwtAuthFilter.java).
- Security rules: [SecurityConfig.java](src/main/java/com/FindMyService/config/SecurityConfig.java).

Typical flow:
1. Register: `POST /api/auth/register`
2. Login: `POST /api/auth/login` → receives `token`
3. Use `Authorization: Bearer <token>` for protected endpoints

## Common Commands
```bash
# Build
./mvnw clean install

# Run
./mvnw spring-boot:run

# Test
./mvnw test

# Format (if Spotless/Checkstyle configured)
./mvnw spotless:apply || ./mvnw checkstyle:check
```

## Configuration
- Application properties: [src/main/resources](src/main/resources)
- Adjust Spring profiles, DB connection, and Swagger settings as needed.

## Endpoints Overview
Controllers define primary routes:
- Auth: [AuthController.java](src/main/java/com/FindMyService/controller/AuthController.java)
- Users: [UserController.java](src/main/java/com/FindMyService/controller/UserController.java)
- Providers: [ProviderController.java](src/main/java/com/FindMyService/controller/ProviderController.java)
- Service Catalog: [ServiceCatalogController.java](src/main/java/com/FindMyService/controller/ServiceCatalogController.java)
- Orders: [OrderController.java](src/main/java/com/FindMyService/controller/OrderController.java)
- Feedback: [FeedbackController.java](src/main/java/com/FindMyService/controller/FeedbackController.java)

Refer to Swagger UI for full request/response schemas.

## Testing
- Unit/integration tests live under [src/test/java/com/FindMyService](src/test/java/com/FindMyService).
- Run:

```bash
./mvnw test
```

## Contributing
- Open an issue or PR with a clear description and steps to reproduce.
- Keep changes focused and aligned with existing style.

## License
- Specify project license if applicable (e.g., MIT/Apache-2.0).