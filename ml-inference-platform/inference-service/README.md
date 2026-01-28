# Inference Service

Python-based gRPC service for ML inference.

## Responsibilities
- Load ML models and scalers at startup (Singleton Registry).
- Expose `PredictBaseRanking` and `PredictDemand` via gRPC.
- Stateless execution (models loaded in memory).

## Running
1. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
2. Generate Protos (from root):
   ```bash
   python -m grpc_tools.protoc -I../proto --python_out=./app --grpc_python_out=./app ../proto/inference.proto
   ```
3. Run:
   ```bash
   python -m app.main
   ```
