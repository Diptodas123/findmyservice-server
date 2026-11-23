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
- Spring Boot 4.2.0
- Spring Security (JWT)
- Maven
- Swagger/OpenAPI
- Docker & Docker Compose
- Kubernetes & Helm
- Doppler (secrets management)

## Project Structure
- `src/main/java/com/FindMyService/`
  - `config/` — app configuration (security, Swagger, exception handling)
  - `controller/` — REST controllers
  - `model/` — entities, DTOs, enums
  - `repository/` — JPA repositories
  - `security/` — JWT filter and utilities
  - `service/` — business logic
  - `utils/` — shared utilities
- `src/test/java/com/FindMyService/` — unit and integration tests

## Getting Started
### Prerequisites
- **Option 1 - Docker (Recommended):**
  - Docker & Docker Compose installed

- **Option 2 - Local:**
  - macOS/Linux/Windows
  - Java 21 installed (verify with `java -version`)
  - Maven

- **Option 3 - Kubernetes:**
  - Docker
  - minikube
  - kubectl
  - Helm
  - Doppler CLI

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
Clone/open the project, then run:
```bash
mvn clean install
```

#### Kubernetes
```bash
# Start minikube
minikube start
minikube addons enable ingress
minikube addons enable metrics-server  # required for HPA memory scaling

# One-time setup
kubectl apply -f k8s/namespace.yaml

# Install Doppler operator (registers the DopplerSecret CRD)
kubectl apply -f https://github.com/DopplerHQ/kubernetes-operator/releases/latest/download/recommended.yaml
kubectl -n doppler-operator-system wait --for=condition=Available deployment/doppler-operator-controller-manager --timeout=60s

kubectl create secret generic doppler-token-secret \
  --namespace findmyservice \
  --from-literal=serviceToken=<your-doppler-service-token>
kubectl apply -f k8s/doppler-secret.yaml

# Deploy
helm upgrade --install findmyservice ./k8s/helm -n findmyservice

# Add hosts entry (one-time)
echo "127.0.0.1 findmyservice.local" | sudo tee -a /etc/hosts

# Start tunnel (keep this terminal open)
minikube service ingress-nginx-controller -n ingress-nginx --url
minikube tunnel     # if the above one doesn't work
```

> **Doppler token**: must be a **project-scoped Service Token** (generated in Doppler dashboard under your project → Access). A personal/CLI token will fail with "You must specify a project".

> **Access**: after tunnel is running, the app is available at `http://findmyservice.local` (port 80, no port number needed).

## Run
Start the application:

```bash
mvn spring-boot:run
```

By default the server runs on `http://localhost:8080`.

## API Docs (Swagger)
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Authentication
JWT-based authentication:

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
mvn clean install

# Run
mvn spring-boot:run

# Test
mvn test
```

## Configuration
Application properties are in `src/main/resources/`. Adjust Spring profiles, DB connection, and Swagger settings as needed.

## Endpoints Overview
- `POST /api/auth/register` — register
- `POST /api/auth/login` — login
- `/api/users/**` — user management
- `/api/providers/**` — provider management
- `/api/services/**` — service catalog
- `/api/orders/**` — orders
- `/api/feedback/**` — feedback

Refer to Swagger UI for full request/response schemas.

## Testing
```bash
mvn test
```

## Contributing
- Open an issue or PR with a clear description and steps to reproduce.
- Keep changes focused and aligned with existing style.

## License
- Specify project license if applicable (e.g., MIT/Apache-2.0).
