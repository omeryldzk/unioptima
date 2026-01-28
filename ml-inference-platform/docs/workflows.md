# Workflows

## Development Workflow

### Adding a New Model version
1. Update `inference-service` to include the new model file.
2. Update `ModelRegistry.py` to load the new model.
3. If input features change, update `inference.proto` and regenerate code.

### Updating the Contract
1. Modify `proto/inference.proto`.
2. Re-run `protoc` generation for Python:
   ```bash
   python -m grpc_tools.protoc -I. --python_out=./inference-service/app --grpc_python_out=./inference-service/app proto/inference.proto
   ```
3. Re-run Maven build for Java (it auto-generates sources):
   ```bash
   cd backend-service && ./mvnw clean compile
   ```
