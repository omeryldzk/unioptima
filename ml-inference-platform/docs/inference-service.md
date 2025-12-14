# Inference Service Documentation

This document provides a detailed overview of the Python Inference Service implementation.

## Artifacts Info
- **Type**: gRPC Server
- **Language**: Python 3.10+
- **Framework**: grpcio, Hugging Face Hub, Scikit-learn

## Architecture Overview

The Inference Service is a dedicated microservice responsible for hosting Machine Learning models and serving predictions via the gRPC protocol. It is stateless but maintains an in-memory cache of heavy model artifacts.

### Key Components

1.  **Model Registry (`app/model_registry.py`)**
    *   **Singleton Pattern**: Ensures models are loaded only once upon application startup.
    *   **Hugging Face Integration**: Automatically downloads model artifacts from private/public Hugging Face repositories using `huggingface_hub`.
    *   **Artifacts Managed**:
        *   **Base Ranking**: Contains multiple sub-models (`uni_clusters/*.joblib`, `prog_clusters/*.joblib`) and a global scaler.
        *   **Demand Forecasting**: Contains a Main model and a Fallback model.
    *   **Lazy/Eager Loading**: Models are downloaded and loaded into memory immediately on instantiation to ensure the service is "ready" when the server starts.

2.  **gRPC Server (`app/main.py`)**
    *   **`ModelServiceServicer`**: Implements the `inference.proto` contract.
    *   **Endpoints**:
        *   `PredictBaseRanking`: Logic delegates to `app/base_ranking.py`. Handles scaling and cluster-specific model selection.
        *   `PredictDemand`: Logic delegates to `app/demand.py`. Handles fallback logic.
    *   **Concurrency**: Uses a `ThreadPoolExecutor` to handle multiple concurrent gRPC requests.

3.  **Prediction Logic**
    *   **Base Ranking (`app/base_ranking.py`)**:
        *   Standardizes input features using the loaded `StandardScaler`.
        *   Averages predictions from a University-Cluster model and a Program-Cluster model.
    *   **Demand (`app/demand.py`)**:
        *   Selects between the "Main" or "Fallback" model based on the request flag.

4.  **Configuration (`app/config.py`)**
    *   Powered by `pydantic-settings`.
    *   **Environment Variables**:
        *   `ML_GRPC_PORT`: Server port (default 50051).
        *   `HF_TOKEN`: Authentication for Hugging Face (Required for private models).
        *   `BASE_RANKING_REPO_ID`, `DEMAND_REPO_ID`, etc.: dynamic repository targets.

## Data Flow

1.  **Startup**: `ModelRegistry` initializes, checks for local cached models, or downloads updates from Hugging Face. Models are deserialized via `joblib`.
2.  **Request**: A gRPC message arrives with a list of `features` (doubles).
3.  **Processing**:
    *   The service identifies the correct model (e.g., by Cluster ID for ranking).
    *   Features are pre-processed (scaled) if necessary.
    *   The `predict()` method of the scikit-learn model is called.
4.  **Response**: The prediction (float) is returned in a `PredictionResponse` protobuf message.
