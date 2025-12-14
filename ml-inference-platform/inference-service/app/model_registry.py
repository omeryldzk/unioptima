import logging
import os
import joblib
from huggingface_hub import snapshot_download, hf_hub_download
from app.config import settings

class ModelRegistry:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(ModelRegistry, cls).__new__(cls)
            cls._instance.models = {
                'uni_clusters': {},
                'prog_clusters': {},
                'demand_main': None,
                'demand_fallback': None
            }
            cls._instance.scalers = {}
            cls._instance.load_models()
        return cls._instance

    def load_models(self):
        logging.info("Loading models from Hugging Face Hub...")
        
        try:
            # 1. Base Ranking Models (Uni & Prog + Scaler)
            logging.info(f"Downloading Base Ranking artifacts from {settings.repo_id_base_ranking}...")
            ranking_path = snapshot_download(
                repo_id=settings.repo_id_base_ranking,
                token=settings.hf_token,
                local_dir=os.path.join(settings.model_path, "base_ranking")
            )
            
            # Load Scaler
            scaler_path = os.path.join(ranking_path, "scaler.joblib")
            if os.path.exists(scaler_path):
                self.scalers['base_scaler'] = joblib.load(scaler_path)
                logging.info(f"Loaded scaler from {scaler_path}")
            
            # Load Uni Clusters
            uni_dir = os.path.join(ranking_path, "uni_clusters")
            if os.path.exists(uni_dir):
                for f in os.listdir(uni_dir):
                    if f.endswith(".joblib"):
                        # format: cluster_{id}_rf.joblib
                        # clean extraction of ID: cluster_123_rf.joblib -> 123
                        try:
                            c_id = f.split('_')[1] # simplistic parsing based on user script
                            self.models['uni_clusters'][c_id] = joblib.load(os.path.join(uni_dir, f))
                        except Exception as e:
                            logging.warning(f"Skipping malformed file {f}: {e}")
            
            # Load Prog Clusters
            prog_dir = os.path.join(ranking_path, "prog_clusters")
            if os.path.exists(prog_dir):
                for f in os.listdir(prog_dir):
                    if f.endswith(".joblib"):
                         try:
                            c_id = f.split('_')[1]
                            self.models['prog_clusters'][c_id] = joblib.load(os.path.join(prog_dir, f))
                         except Exception as e:
                            logging.warning(f"Skipping malformed file {f}: {e}")

            # 2. Demand Main Model
            logging.info(f"Downloading Demand Main from {settings.repo_id_demand}...")
            demand_path = hf_hub_download(
                repo_id=settings.repo_id_demand,
                filename="model_v1/model.joblib",
                token=settings.hf_token,
                local_dir=os.path.join(settings.model_path, "demand_main")
            )
            self.models['demand_main'] = joblib.load(demand_path)

            # 3. Demand Fallback Model
            logging.info(f"Downloading Demand Fallback from {settings.repo_id_demand_fallback}...")
            fallback_path = hf_hub_download(
                repo_id=settings.repo_id_demand_fallback,
                filename="model_v1/model.joblib",
                token=settings.hf_token,
                local_dir=os.path.join(settings.model_path, "demand_fallback")
            )
            self.models['demand_fallback'] = joblib.load(fallback_path)

            logging.info("All models loaded successfully.")
            
        except Exception as e:
            logging.error(f"Failed to load models: {e}")
            # In production, we might want to crash if models fail to load
            # raise e

    def get_uni_model(self, cluster_id):
        return self.models['uni_clusters'].get(str(cluster_id))

    def get_prog_model(self, cluster_id):
        return self.models['prog_clusters'].get(str(cluster_id))

    def get_demand_model(self, use_fallback=False):
        return self.models['demand_fallback'] if use_fallback else self.models['demand_main']

    def get_scaler(self, name):
        return self.scalers.get(name)
