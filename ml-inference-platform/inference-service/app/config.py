import os
from pydantic_settings import BaseSettings
from pydantic import Field

class Settings(BaseSettings):
    service_name: str = "inference-service"
    grpc_port: int = 50051
    model_dir: str = "/app/models"
    
    hf_token: str = Field(..., description="Hugging Face access token")
    repo_id_base_ranking: str = Field(..., description="Base ranking model repository ID")
    repo_id_demand: str = Field(..., description="Demand model repository ID")
    repo_id_demand_fallback: str = Field(..., description="Demand fallback model repository ID")

    class Config:
        env_prefix = "ML_"
        protected_namespaces = ('settings_',)

settings = Settings()
