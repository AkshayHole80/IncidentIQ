# IncidentIQ: Microservices IT Incident Management System

IncidentIQ is a robust, production-ready IT Incident Management platform designed for reporting, classifying, tracking, and resolving IT issues. The system features automatic incident classification (Priority and Category) powered by a Groq Large Language Model, secure file attachment handling via Amazon S3, auditing trails, and real-time user notification dispatching.

---

## 1. Project Architecture

The application is built on a distributed microservices pattern. Backend microservices are built with **Spring Boot** and **Java 21**, registering automatically with a **Netflix Eureka** discovery server. Clients route their REST API requests through **Spring Cloud Gateway**. 

```text
                      +-------------------+
                      |  User / Browser   |
                      +---------+---------+
                                |
             +------------------+------------------+
             | (HTTP :3000/80)                     | (WS :8084)
             v                                     v
   +---------+---------+                 +---------+---------+
   |  React Frontend   |                 | WebSocket /ws     |
   +---------+---------+                 +---------+---------+
             | (HTTP :8080)                        ^
             v                                     | (WS Broadcast)
   +---------+---------+                 +---------+---------+
   |   API Gateway     |                 |   Notification    |
   +----+----+----+----+                 |     Service       |
        |    |    |                      +---------+---------+
        |    |    +---------------+                ^
        |    |                    |                | (Kafka Event)
        |    v                    v                |
        |  [User Service]   [AI Service]           |
        |    (:8081)             (:8083)           |
        v                                          |
   [Incident Service] -----------------------------+
        (:8082)
```


---
## 2. Key Features

- **Role-Based Workflows**: Tailored dashboard statistics and operation restrictions for **Users** (report tickets, view notifications), **Support Engineers** (claim tickets, update lifecycle states, submit resolutions), and **Admins** (re-assign tickets, close tickets, monitor system health).
- **AI-Powered Classification**: Automatically analyzes incident titles and descriptions on submission to predict priority and IT category using the Groq Completions API (`llama-3.3-70b-versatile`). Includes automatic fail-safes.
- **Event-Driven Architecture**: Uses Apache Kafka to publish state transitions asynchronously from `incident-service` to `notification-service`.
- **Real-Time Live Notifications**: Employs WebSockets (SockJS & STOMP protocol) inside `notification-service` to broadcast instant alerts to active user browsers.
- **Secure File Storage**: Direct file attachments are uploaded to an AWS S3 bucket, with security enforced by serving expiring pre-signed URLs for downloads and views.
- **Comprehensive Auditing**: Tracks all state changes (Creation, Assignment, Resolution, Closure) with user attributes inside a dedicated audit log DB table.

---

## 3. Technology Stack

### Backend
- Java 21 & Spring Boot 3.5+
- Spring Cloud Gateway (WebFlux)
- Spring Cloud Netflix Eureka Server & OpenFeign Clients
- Spring Data JPA (Hibernate)
- Spring Security (JWT authentication)
- Spring Kafka & Spring WebSocket

### Frontend
- React 19 & React Router DOM 7
- Vite & Tailwind CSS v4
- Ant Design v6 components
- Recharts (statistics visualizers)
- SockJS-client & StompJS (Websocket handler)

### Databases & Middleware
- PostgreSQL (AWS RDS instance in production)
- Redis (Session cache & user data caching)
- Apache Kafka (Event message broker)

---

## 4. Directory Structure

```text
IncidentIQ/
├── eureka-server/           # Netflix Eureka Service Registry
├── gatway-service/          # Spring Cloud API Gateway (Routing and CORS)
├── user-service/            # Authentication, JWT, and User Profiles
├── incident-service/        # Incident lifecycle, S3 attachments, and Audit Logs
├── ai-service/              # LLM Groq Classification API
├── notification-service/    # Kafka event subscriber and WebSocket Broadcaster
├── IncidentIQ-frontend/     # React Client SPA (served via Nginx in Docker)
├── docker-compose.yml       # Production-ready Docker orchestrator
├── docker-compose-local.yml # Local development orchestrator
├── .env.example             # Template for required environment variables
└── .env.docker              # Environment configuration for containers
```

---

## 5. Port Mappings Matrix

| Service | Host Port | Internal Container Port | Description |
| :--- | :--- | :--- | :--- |
| **eureka-server** | `8761` | `8761` | Discovery dashboard URL |
| **gateway-service** | `8080` | `8080` | Entrypoint for all backend API routes |
| **user-service** | `8081` | `8081` | Direct access to Auth & Users |
| **incident-service** | `8082` | `8082` | Direct access to Incidents & Attachments |
| **ai-service** | `8083` | `8083` | Direct access to Groq LLM Classifier |
| **notification-service**| `8084` | `8084` | Direct access to notifications and `/ws` WebSocket endpoint |
| **frontend** | `3000` | `80` | Nginx React client server |
| **redis** | `6379` | `6379` | Shared cache container |
| **kafka** | `9092` | `9092` | Event broker |

---

## 6. Local Development Setup

### Prerequisites
- Docker & Docker Compose installed
- JDK 21 and Maven (if running backend services outside Docker)
- Node.js & npm (if running frontend outside Docker)

### Running Everything with Docker Compose

1. **Clone the repository** and navigate to the project root.
2. **Configure environment variables**:
   Create a `.env` file based on `.env.example`:
   ```bash
   cp .env.example .env
   ```
   Provide valid values for the databases, AWS credentials, S3 bucket names, and your **Groq API key**.

3. **Start the containers**:
   ```bash
   docker compose -f docker-compose.yml up -d --build
   ```
4. **Access the application**:
   - Frontend UI: `http://localhost:3000`
   - Eureka Discovery Dashboard: `http://localhost:8761`
   - Gateway endpoint: `http://localhost:8080`

### Database Schema Updates
The backend services use Hibernate's `ddl-auto: update` configuration. When the services start, Hibernate will automatically connect to your database and generate or update the required tables (`users`, `incidents`, `attachments`, `audit_logs`, `notifications`).

---

## 7. Production Configurations

### CORS Settings
The Gateway handles CORS globally. In production, configure the allowed origins in `application.yml` or using the `FRONTEND_URL` environment variable:
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOriginPatterns:
              - "*"  # Allows dynamic public IPs or custom domain mappings
            allowedMethods:
              - GET
              - POST
              - PUT
              - PATCH
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
```

### Production Nginx Reverse Proxy
In production, the frontend Nginx reverse-proxies API calls to the Gateway and WebSocket connections to the Notification Service:
```nginx
# API Gateway Proxy
location /api/ {
    proxy_pass http://gateway-service:8080;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
}

# WebSocket STOMP Proxy to Notification Service
location /ws {
    proxy_pass http://notification-service:8084;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "Upgrade";
    proxy_set_header Host $host;
}
```
