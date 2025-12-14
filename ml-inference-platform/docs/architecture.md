# System Architecture

The ML Inference Platform is composed of two main services and a shared contract.

## Components

### 1. Spring Boot Backend Service (Orchestrator)
- **Role**: Entry point for business logic and data retrieval.
- **Data Source**: MongoDB Atlas (`encoded_data` collection).
- **Responsibilities**:
  - Fetches feature data.
  - Resolves versioning (latest year suffix).
  - Subsets features based on metadata.
  - Calls Inference Service via gRPC.

### 2. FastAPI/gRPC Inference Service (Python)
- **Role**: Pure inference computation.
- **Data Source**: None (Models loaded in memory at startup).
- **Responsibilities**:
  - Hosts ML models (Singleton Registry).
  - Performs scaling and model prediction.
  - Agnostic of database schema.

### 3. Shared Contract (Proto)
- Defined in `proto/inference.proto`.
- Single source of truth for API definitions.

## Data Flow
1. **Request**: Backend receives a request for ranking/demand.
2. **Fetch**: Backend queries MongoDB for `_id` prefix.
3. **Resolve**: `LatestYearResolver` picks the newest document.
4. **Prepare**: Features are subsetted.
5. **Call**: gRPC request sent to Inference Service.
6. **Predict**: Inference Service scales features, runs models, returns prediction.
7. **Response**: Backend returns result.
