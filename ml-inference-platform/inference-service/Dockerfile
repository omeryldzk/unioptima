FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app/ ./app/
# In a real build, we would copy generated protos here or generate them
# COPY proto_gen/ ./inference_pb2.py
# COPY proto_gen/ ./inference_pb2_grpc.py

# Placeholder for proto generation if we were doing it inside build (optional)
# RUN python -m grpc_tools.protoc -I../proto --python_out=. --grpc_python_out=. ../proto/inference.proto

ENV PYTHONPATH=/app
CMD ["python", "-m", "app.main"]
