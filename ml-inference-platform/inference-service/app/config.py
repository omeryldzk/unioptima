import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    service_name: str = "inference-service"
    grpc_port: int = 50051
    model_path: str = "/app/models"
    
    hf_token: str = os.getenv("HF_TOKEN")
    repo_id_base_ranking: str = os.getenv("BASE_RANKING_REPO_ID")
    repo_id_demand: str = os.getenv("DEMAND_REPO_ID")
    repo_id_demand_fallback: str = os.getenv("DEMAND_FALLBACK_REPO_ID")

    class Config:
        env_prefix = "ML_"

settings = Settings()
