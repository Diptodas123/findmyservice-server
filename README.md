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
- Java 21 (OpenJDK)
- Spring Boot 3.5.7
- Spring Security (JWT)
- Maven (with `mvnw` wrapper)
- Swagger/OpenAPI
- Docker & Docker Compose

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
- **Option 1 - Docker (Recommended):**
  - Docker & Docker Compose installed
  
- **Option 2 - Local:**
  - macOS/Linux/Windows
  - Java 21 installed (verify with `java -version`)
  - Maven (or use included `mvnw` wrapper)

### Setup & Run

#### Using Docker (Recommended)
Build and run with Docker Compose:

```bash
docker-compose up --build
```

Or build and run manually:

```bash
# Build image
docker build -t findmyservice:latest .

# Run container
docker run -p 8080:8080 findmyservice:latest
```

#### Local Development
Clone/open the project, then use the Maven wrapper:

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

### Docker Commands
```bash
# Build and run with compose
docker-compose up --build

# Stop containers
docker-compose down

# View logs
docker-compose logs -f

# Rebuild image
docker build -t findmyservice:latest .

# Run container with custom port
docker run -p 9090:8080 findmyservice:latest
```

### Local Development
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