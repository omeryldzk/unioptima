# Start from Python 3.11 slim
FROM python:3.11-slim

# Set the working directory
WORKDIR /service

# 1. Install system dependencies (optional, but good for debugging)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# 2. Install Python dependencies
# Ensure 'grpcio-tools' is in your requirements.txt!
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 3. Copy the 'app' folder
COPY app/ ./app/

# 4. GENERATE PROTOS INSIDE DOCKER (The Fix)
# This command ensures the files exist exactly where Python expects them.
# We create the __init__.py to ensure it's treated as a package if needed.
RUN touch app/generated/__init__.py
RUN python -m grpc_tools.protoc \
    -I./app/proto \
    --python_out=./app/generated \
    --grpc_python_out=./app/generated \
    ./app/proto/inference.proto

# 5. Setup PYTHONPATH
# Allows "import inference_pb2" to work from anywhere
ENV PYTHONPATH=/service:/service/app/generated

# 6. Start the server
CMD ["python", "-m", "app.main"]