import logging
import grpc
from concurrent import futures
import sys
import os

# Add parent directory to path so we can import generated protos if they are there
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from app.config import settings
from app.model_registry import ModelRegistry
from app import base_ranking, demand

# NOTE: these imports assume code generation has run
try:
    import inference_pb2
    import inference_pb2_grpc
except ImportError:
    # Fallback for when protos aren't generated yet (to avoid immediate crashing during skeleton review)
    # in production this should crash
    logging.warning("Proto files not found. Ensure protoc has been run.")
    inference_pb2 = None
    inference_pb2_grpc = None

class ModelServiceServicer(inference_pb2_grpc.ModelServiceServicer if inference_pb2_grpc else object):
    def PredictBaseRanking(self, request, context):
        try:
            predictions = base_ranking.predict_ranking(
                request.uni_cluster_id, 
                request.prog_cluster_id, 
                list(request.features)
            )
            return inference_pb2.PredictionResponse(predictions=predictions)
        except Exception as e:
            logging.error(f"Error in PredictBaseRanking: {e}")
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            return inference_pb2.PredictionResponse()

    def PredictDemand(self, request, context):
        try:
            predictions = demand.predict_demand(
                request.use_fallback, 
                list(request.features)
            )
            return inference_pb2.PredictionResponse(predictions=predictions)
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
