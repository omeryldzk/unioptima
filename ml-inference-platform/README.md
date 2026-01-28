# ML Inference Platform

A production-grade monorepo for ML inference orchestration, combining high-performance Java Spring Boot backend services with Python-based ML inference.

## Architecture

The platform consists of the following modular services orchestrated via Docker Compose:

- **Frontend**: A React + Vite application for user interaction (Search, Simulation).
- **Backend Service**: Java Spring Boot (v3.2, Java 21) orchestration service handling business logic, data retrieval, and gRPC communication with the inference layer.
- **Inference Service**: Python FastAPI service hosting ML models, communicating via gRPC.
- **Typesense**: Open source, typo-tolerant search engine for fast data retrieval.
- **Observability Stack**:
    - **Prometheus**: Metrics collection.
    - **Grafana**: Visualization dashboards.
    - **Loki**: Log aggregation.
- **Nginx**: Reverse proxy handling routing to the frontend and other services.

## Prerequisites

- [Docker](https://www.docker.com/)
- [Docker Compose](https://docs.docker.com/compose/)

## Quick Start

1.  **Clone the repository**:
    ```bash
    git clone <repository-url>
    cd ml-inference-platform
    ```

2.  **Start the entire stack**:
    ```bash
    docker-compose up --build
    ```

3.  **Access the application services**:

| Service | URL | Description | Credentials (if any) |
| :--- | :--- | :--- | :--- |
| **Frontend** | [http://localhost:80](http://localhost:80) | Main User Interface | - |
| **Grafana** | [http://localhost:3000](http://localhost:3000) | Monitoring Dashboards | `admin` / `admin` |
| **Prometheus** | [http://localhost:9090](http://localhost:9090) | Metrics Scraper | - |
| **Typesense** | [http://localhost:8108](http://localhost:8108) | Search Engine API | - |

## Development Stack

### Backend (`/backend-service`)
- **Language**: Java 21
- **Framework**: Spring Boot 3.2
- **Key Libraries**: gRPC (Net.devh), Spring Data MongoDB, Micrometer Tracing.
- **Database**: MongoDB Atlas (Configured via `SPRING_DATA_MONGODB_URI`).

### Frontend (`/frontend`)
- **Framework**: React
- **Build Tool**: Vite
- **Styling**: CSS / TailwindCSS (if applicable)

### Inference Service (`/inference-service`)
- **Language**: Python
- **Framework**: FastAPI (gRPC)
- **ML Integration**: Hugging Face Hub

### Storage & Search
- **MongoDB Atlas**: Primary persistent storage.
- **Typesense**: Search and filtering engine.

## Project Structure

- `proto/`: Shared gRPC Protocol Buffers definitions.
- `backend-service/`: Java Spring Boot backend application.
- `inference-service/`: Python ML inference service context.
- `frontend/`: React frontend application source.
- `nginx/`: Nginx configuration.
- `docker-compose.yml`: Root service orchestration.
- `docs/`: Architecture and decision records.
