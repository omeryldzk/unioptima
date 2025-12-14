# Backend Service Documentation

This document provides a detailed overview of the Backend Service implementation. It is designed to be updated with every commit to reflect the current state of the service.

## Artifacts Info
- **Type**: Spring Boot Application
- **Language**: Java 21
- **Build Tool**: Maven

## Architecture Overview

The Backend Service acts as the orchestrator and data layer for the ML Inference Platform. It connects to MongoDB Atlas for persistence and communicates with the Python Inference Service via gRPC for model predictions.

### Key Components

1.  **Data Layer (MongoDB)**
    *   **Repositories**: Extends `MongoRepository`. Custom methods implemented for retrieval logic (e.g., `findTopByIdStartingWithOrderByIdDesc`).
    *   **Dynamic Models**: The data models (`EncodedBaseRankingData`, `EncodedDemandData`, `RawData`) are designed with a **Dynamic Map** pattern.
        *   Core identification fields (`id`, `academicYear`, `idOSYM`) are explicit.
        *   All other feature columns (which may vary or be numerous) are captured in a `Map<String, Object> extraFields`.
    *   **Custom Converters**: 
        *   `AbstractDynamicFieldConverter`: A generic base class that handles the mapping of standard BSON documents into the dynamic POJOs. It iterates over the Document keys and populates the `extraFields` map, excluding known fields.
        *   `MongoConfig`: Registers these custom converters (`EncodedDemandDataReadConverter`, etc.) with Spring Data MongoDB.

2.  **Service Layer**
    *   **Metadata Services** (`DemandMetadataService`, `BaseRankingMetadataService`):
        *   Responsible for loading model metadata (e.g., list of feature names) from MongoDB on startup (`@PostConstruct`).
        *   Caches this metadata in memory to avoid repeated DB lookups during inference requests.
    *   **Prediction Services** (`DemandService`, `BaseRankingService`):
        *   Orchestrates the flow: Fetch Metadata -> Fetch Latest Feature Data (from Repo) -> Extract Feature Vector -> Call gRPC.
        *   **gRPC Client**: Uses `net.devh.grpc.client` to inject the `ModelServiceBlockingStub` for communicating with the Inference Service.

3.  **Configuration**
    *   **`application.properties`**:
        *   `spring.data.mongodb.uri`: Externalized via `SPRING_DATA_MONGODB_URI` (or `ATLAS_URI`).
        *   `spring.grpc.client.channels.inferenceService`: Configured to point to the `inference-service` host.
    *   This setup allows the backend to be environment-agnostic (local Docker vs. production).

## Data Flow

1.  **Startup**: Metadata services load feature definitions (e.g., "Which columns does the Demand Model need?") from `demand_metadata` / `baseRanking_metadata` collections.
2.  **Request**: (Triggered via API or internal scheduler - *TBD*).
3.  **Data Retrieval**: Service queries `Encoded*DataRepository` for the latest record matching a specific `idOSYM` prefix.
4.  **Feature Extraction**: The dynamic `extraFields` map is queried for the specific features required by the model.
5.  **Inference**: A gRPC request (`PredictDemand` or `PredictBaseRanking`) containing the feature vector is sent to the Inference Service.
6.  **Response**: The prediction result is returned.
