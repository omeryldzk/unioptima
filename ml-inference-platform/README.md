# ML Inference Platform

A production-grade monorepo for ML inference orchestration.

## Structure
- `proto/`: Shared gRPC contract.
- `inference-service/`: Python/FastAPI gRPC service for model inference.
- `backend-service/`: Java Spring Boot service for orchestration and data retrieval.
- `docs/`: Architecture and decision records.

## Quick Start (Docker)
1. Ensure Docker and Docker Compose are installed.
2. Run the platform:
   ```bash
   docker-compose up --build
   ```
3. Services will be available at:
   - Backend: localhost:8080
   - Inference (gRPC): localhost:50051

## Development
See [docs/workflows.md](docs/workflows.md) for detailed development workflows.
