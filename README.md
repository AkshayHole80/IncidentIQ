#  IncidentIQ: Enterprise Microservices IT Incident Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5+-green.svg?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-blue.svg?style=for-the-badge&logo=react)](https://react.dev)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-latest-black.svg?style=for-the-badge&logo=apachekafka)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg?style=for-the-badge&logo=redis)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg?style=for-the-badge&logo=docker)](https://www.docker.com/)
[![AWS S3](https://img.shields.io/badge/AWS%20S3-Bucket-orange.svg?style=for-the-badge&logo=amazons3)](https://aws.amazon.com/s3/)

IncidentIQ is a production-ready, distributed IT Incident Management system designed to streamline the reporting, assignment, tracking, and resolution of organizational IT issues. The platform integrates modern technologies like dynamic AI ticket classification, asynchronous event-driven messaging, secure object storage, real-time WebSocket notifications, and high-performance user caching.

---

##  Project Overview

IncidentIQ automates the IT support ticket lifecycle. Instead of manual triage, incoming incidents are analyzed by LLMs to determine their category and priority. Workflows are tailored dynamically for three roles: regular employees, support engineers, and admins. All inter-service activities are securely audited, and notifications are delivered instantaneously to active web browsers.

---

## Features

*   **Role-Based Workflows:** Distinct UI dashboards and business rule enforcements for:
   *   **Users:** Log tickets, attach files, view personal ticket status, read notifications.
   *   **Support Engineers:** Claims tickets, updates status, registers resolution notes.
   *   **Admins:** Overlooks all system statistics, assigns tickets, closes tickets, monitors audit trails.
*   **AI-Powered Ticket Classification:** Classifies tickets' priority (`LOW` to `CRITICAL`) and category (`NETWORK`, `DATABASE`, etc.) instantly upon submission using Groq's Llama 3.3 model.
*   **Decoupled Event Notifications:** Utilizes Apache Kafka to asynchronously publish incident state changes to a notification processor.
*   **Live Notification Pushes:** Uses WebSockets (STOMP over SockJS) to broadcast alerts to the user interface in real-time.
*   **AWS S3 Secure Storage:** Directly uploads ticket attachments to Amazon S3, securing access by generating expiring pre-signed URLs.
*   **State Auditing:** Automatically logs every stage of the incident lifecycle (created, assigned, resolved, closed) with user context to a central audit repository.

---

## 📐 System Architecture

The following diagram illustrates the microservices topology, service discoveries, and message buses:



<summary> Click to expand Text-based ASCII Diagram Fallback</summary>

```text
                               +-----------------------------+
                               |    Web Browser / Client     |
                               +--------------+--------------+
                                              |
                     +------------------------+------------------------+
                     | (HTTP port 3000)                                | (WebSockets port 8084)
                     v                                                 v
         +-----------------------+                         +-----------------------+
         |     React Frontend    |                         |    WebSocket (/ws)    |
         +-----------+-----------+                         +-----------+-----------+
                     | (HTTP API Calls)                                ^
                     v                                                 | (Live Broadcasts)
         +-----------------------+                         +-----------+-----------+
         |  Spring API Gateway   |                         | Notification Service  |
         +-----+-----+-----+-----+                         +-----------+-----------+
               |     |     |                                           ^
               |     |     +----------------+                          |
               |     v                      v                          | (Kafka Messages)
               |  [User Service]       [AI Service]                    |
               |  (Redis Cache :6379)  (Groq LLM API)                  |
               v                                                       |
         [Incident Service] ───────────────────────────────────────────+
         (AWS S3 File Storage)
```




##  AI Integration

When an incident is created, `incident-service` uses a Feign client to request classification from `ai-service`.

*   **LLM Model:** Llama 3.3 (`llama-3.3-70b-versatile`) via Groq API.
*   **Prompt Constraints:** The LLM is instructed to return **strictly valid JSON** containing the recommended priority (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) and category (`APPLICATION`, `DATABASE`, `NETWORK`, `SECURITY`, `INFRASTRUCTURE`).
*   **Fault Tolerance:** The service tries calling Groq up to **3 times** with a 2-second delay. If all attempts fail, it applies a safe fallback classification (`MEDIUM` priority, `APPLICATION` category) so ticket creation never crashes.

---

##  Authentication Flow

Security is stateless and uses JSON Web Tokens (JWT).

1.  **Generation:** The user logs in via `/api/v1/auth/login`. The `user-service` hashes user input, verifies it against PostgreSQL (using `BCrypt`), and generates a JWT signed with a secret key.
2.  **Verification:** The client passes this token in the header as `Authorization: Bearer <token>` for subsequent requests. Downstream services (like `incident-service`) intercept the call and validate the token locally using the same shared secret key.
3.  **Token Propagation:** When `incident-service` needs to query `user-service` via Feign, the `RequestInterceptor` in `FeignConfig` copies the incoming client authentication token and appends it to the outgoing inter-service request, propagating the user's role context.

---

##  Deployment & CI/CD Architecture

### CI/CD Workflow
The CI/CD pipeline is orchestrated using **GitHub Actions** (`.github/workflows/cicd.yml`):
1.  **Build:** On pushes to `main` branch, a runner checks out the code, configures Java 21 Temurin, and builds each Maven package.
2.  **Push:** Builds Docker images for the frontend and all 5 backend services, tagging and pushing them to DockerHub.
3.  **Deploy:** The runner logs into the AWS EC2 instance via SSH, pulls the latest code and docker images, stops old containers, restarts updated containers, and runs Actuator healthchecks.

### Port Mappings Matrix
| Service | Container Port | External Port |
| :--- | :--- | :--- |
| Gateway | `8080` | `8080` |
| Eureka Dashboard | `8761` | `8761` |
| Frontend UI | `80` | `3000` |
| Redis Cache | `6379` | `6379` |
| Kafka Broker | `9092` | `9092` |

---

## 📂 Project Structure

```text
IncidentIQ/
├── .github/workflows/       # GitHub Actions CI/CD pipeline configurations
├── eureka-server/           # Service discovery registry
├── gatway-service/          # Spring Cloud routing Gateway
├── user-service/            # Authentication & JWT security profiles
├── incident-service/        # Incident core logic, S3 buckets, & audit logs
├── ai-service/              # Groq completions categorization API
├── notification-service/    # Kafka listener & WebSocket stomp broadcaster
├── IncidentIQ-frontend/     # React 19 Frontend application
├── docker-compose.yml       # Production-ready orchestrator
├── docker-compose-local.yml # Local development orchestrator
├── .env.example             # Template env config file
└── .env.docker              # Environment configuration for containers
```

---

##  Local Setup Instructions

### Prerequisites
*   Java 21 installed.
*   Node.js 18+ and npm installed.
*   PostgreSQL running locally.
*   Redis running locally.
*   Kafka running locally.

### Running Backend Services Individually
Navigate into any microservice directory (e.g. `user-service`), configure its `application.yml` with your local database details, and run:
```bash
./mvnw spring-boot:run
```

### Running Frontend Client
Navigate into `IncidentIQ-frontend` and run:
```bash
npm install
npm run dev
```

---

## 🐳 Docker Setup

The easiest way to run the entire cluster locally is using Docker Compose:

1.  **Create `.env` file:**
    ```bash
    cp .env.example .env
    ```
    Populate the variables with your AWS S3 details and your Groq API key (`GROQ_API_KEY`).

2.  **Build and run the stack:**
    ```bash
    docker compose up -d --build
    ```

3.  **Validate Services:**
   *   Eureka Console: `http://localhost:8761`
   *   React Client Dashboard: `http://localhost:3000`

---

## AWS Deployment

The application is deployed on an **AWS EC2** instance, utilizing **AWS S3** for attachment storage and optionally **AWS RDS** for managed PostgreSQL databases.

### S3 Permissions Setup
Ensure the S3 Bucket has a CORS configuration allowing your production domain to issue uploads:
```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": ["ETag"]
  }
]
```

---

##  Future Enhancements
*   **Kubernetes Migration:** Package services inside a Kubernetes cluster (using Helm charts) to support auto-scaling under high ticketing loads.
*   **Advanced AI SLA Tracking:** Implement LLM-based timeline predictions to warn administrators if a ticket is at risk of breaching Service Level Agreements (SLA).
*   **Consolidated Log Aggregation:** Add an ELK stack (Elasticsearch, Logstash, Kibana) or Prometheus/Grafana dashboard for aggregated log tracking and monitoring.
