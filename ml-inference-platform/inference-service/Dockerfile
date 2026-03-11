# Start from Python 3.11 slim
FROM python:3.11-slim

# Set the working directory
WORKDIR /service

# 1. Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    wget \
    && rm -rf /var/lib/apt/lists/*

RUN wget -qO/bin/grpc_health_probe https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/v0.4.34/grpc_health_probe-linux-amd64 && \
    chmod +x /bin/grpc_health_probe

# 2. Install Python dependencies
# We now copy from 'inference-service/' because context is at the root
COPY inference-service/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 3. Copy the 'app' folder
# Path now includes 'inference-service/'
COPY inference-service/app/ ./app/

# 4. Copy the Shared Proto
# Copy the proto from the root 'proto' folder into the container
COPY proto/ ./app/proto/

# 5. GENERATE PROTOS INSIDE DOCKER 
# Create the output directory and init file
RUN mkdir -p app/generated && touch app/generated/__init__.py

# Generate the code
# -I./app/proto: Look for imports in the container's proto folder
# ./app/proto/inference.proto: The specific file to compile
RUN python -m grpc_tools.protoc \
    -I./app/proto \
    --python_out=./app/generated \
    --grpc_python_out=./app/generated \
    ./app/proto/inference.proto

# 6. Setup PYTHONPATH
ENV PYTHONPATH=/service:/service/app/generated

# 7. Start the server
CMD ["python", "-m", "app.main"]