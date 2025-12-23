import logging
import grpc
from concurrent import futures
import sys
import os

# --- PATH CONFIGURATION ---
# Get the directory where main.py is located (e.g., /service/app)
current_dir = os.path.dirname(os.path.abspath(__file__))

# Construct path to the 'generated' folder (e.g., /service/app/generated)
generated_dir = os.path.join(current_dir, "generated")

# Add 'generated' to sys.path so Python can find 'inference_pb2'
sys.path.append(generated_dir)
# ---------------------------

from app.config import settings
from app.model_registry import ModelRegistry
from app import base_ranking, demand

try:
    import inference_pb2
    import inference_pb2_grpc
except ImportError:
    logging.warning("Proto files not found. Ensure protoc has been run.")
    inference_pb2 = None
    inference_pb2_grpc = None

class ModelServiceServicer(inference_pb2_grpc.ModelServiceServicer if inference_pb2_grpc else object):
    def PredictBaseRanking(self, request, context):
        try:
            prediction = base_ranking.predict_ranking(
                request.uni_cluster_id, 
                request.prog_cluster_id, 
                list(request.features)
            )
            return inference_pb2.PredictionResponse(prediction=prediction)
        except Exception as e:
            logging.error(f"Error in PredictBaseRanking: {e}")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return inference_pb2.PredictionResponse()

    def PredictDemand(self, request, context):
        try:
            prediction = demand.predict_demand(
                request.use_fallback, 
                list(request.features)
            )
            return inference_pb2.PredictionResponse(prediction=prediction)
        except Exception as e:
            logging.error(f"Error in PredictDemand: {e}")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return inference_pb2.PredictionResponse()

def serve():
    logging.basicConfig(level=logging.INFO)
    
    # Initialize implementation
    if not inference_pb2_grpc:
        logging.error("Cannot start server without generated proto files.")
        return

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    inference_pb2_grpc.add_ModelServiceServicer_to_server(ModelServiceServicer(), server)
    
    # Initialize models
    ModelRegistry()
    
    address = f"[::]:{settings.grpc_port}"
    server.add_insecure_port(address)
    logging.info(f"Starting Inference Service on {address}")
    server.start()
    server.wait_for_termination()

if __name__ == "__main__":
    serve()
