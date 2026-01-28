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
        logging.info("Initializing model loading sequence...")
        
        try:
            # =========================================================
            # 1. Base Ranking Models (Uni & Prog + Scaler)
            # =========================================================
            base_ranking_dir = os.path.join(settings.model_dir, "base_ranking")
            
            # CHECK LOCAL FIRST: Only download if the directory doesn't exist
            if not os.path.exists(base_ranking_dir):
                logging.info(f"Local files not found. Downloading Base Ranking from {settings.repo_id_base_ranking}...")
                snapshot_download(
                    repo_id=settings.repo_id_base_ranking,
                    token=settings.hf_token,
                    local_dir=base_ranking_dir
                )
            else:
                logging.info(f"Found local Base Ranking models at {base_ranking_dir}. Skipping download.")

            # Load Scaler
            scaler_path = os.path.join(base_ranking_dir, "scaler.joblib")
            if os.path.exists(scaler_path):
                self.scalers['base_scaler'] = joblib.load(scaler_path)
                logging.info(f"Loaded Base Ranking scaler from {scaler_path}")
            
            # Load Uni Clusters
            uni_dir = os.path.join(base_ranking_dir, "uni_clusters")
            if os.path.exists(uni_dir):
                for f in os.listdir(uni_dir):
                    if f.endswith(".joblib"):
                        try:
                            c_id = f.split('_')[1]
                            self.models['uni_clusters'][c_id] = joblib.load(os.path.join(uni_dir, f))
                            logging.info(f"Loaded Uni Cluster {c_id} from {os.path.join(uni_dir, f)}")
                        except Exception as e:
                            logging.warning(f"Skipping malformed file {f}: {e}")
            
            # Load Prog Clusters
            prog_dir = os.path.join(base_ranking_dir, "prog_clusters")
            if os.path.exists(prog_dir):
                for f in os.listdir(prog_dir):
                    if f.endswith(".joblib"):
                         try:
                            c_id = f.split('_')[1]
                            self.models['prog_clusters'][c_id] = joblib.load(os.path.join(prog_dir, f))
                            logging.info(f"Loaded Prog Cluster {c_id} from {os.path.join(prog_dir, f)}")
                         except Exception as e:
                            logging.warning(f"Skipping malformed file {f}: {e}")

            # =========================================================
            # 2. Demand Main Model
            # =========================================================
            demand_main_dir = os.path.join(settings.model_dir, "demand_main")
            model_path_main = os.path.join(demand_main_dir, "model_v1/model.joblib")
            scaler_path_main = os.path.join(demand_main_dir, "model_v1/scaler.joblib")

            # CHECK LOCAL FIRST
            if not os.path.exists(model_path_main) or not os.path.exists(scaler_path_main):
                logging.info(f"Local files not found. Downloading Demand Main from {settings.repo_id_demand}...")
                hf_hub_download(
                    repo_id=settings.repo_id_demand,
                    filename="model_v1/model.joblib",
                    token=settings.hf_token,
                    local_dir=demand_main_dir
                )
                hf_hub_download(
                    repo_id=settings.repo_id_demand,
                    filename="model_v1/scaler.joblib",
                    token=settings.hf_token,
                    local_dir=demand_main_dir
                )
            else:
                 logging.info(f"Found local Demand Main model. Skipping download.")

            # Load
            self.models['demand_main'] = joblib.load(model_path_main)
            self.scalers['demand_main_scaler'] = joblib.load(scaler_path_main)
            logging.info(f"Loaded Demand Main model from {model_path_main}")

            # =========================================================
            # 3. Demand Fallback Model
            # =========================================================
            demand_fallback_dir = os.path.join(settings.model_dir, "demand_fallback")
            model_path_fallback = os.path.join(demand_fallback_dir, "model_v1/model.joblib")
            scaler_path_fallback = os.path.join(demand_fallback_dir, "model_v1/scaler.joblib")

            # CHECK LOCAL FIRST
            if not os.path.exists(model_path_fallback) or not os.path.exists(scaler_path_fallback):
                logging.info(f"Local files not found. Downloading Demand Fallback from {settings.repo_id_demand_fallback}...")
                hf_hub_download(
                    repo_id=settings.repo_id_demand_fallback,
                    filename="model_v1/model.joblib",
                    token=settings.hf_token,
                    local_dir=demand_fallback_dir
                )
                hf_hub_download(
                    repo_id=settings.repo_id_demand_fallback,
                    filename="model_v1/scaler.joblib",
                    token=settings.hf_token,
                    local_dir=demand_fallback_dir
                )
            else:
                logging.info(f"Found local Demand Fallback model. Skipping download.")

            # Load
            self.models['demand_fallback'] = joblib.load(model_path_fallback)
            self.scalers['demand_fallback_scaler'] = joblib.load(scaler_path_fallback)

            logging.info("All models loaded successfully.")
            
        except Exception as e:
            logging.error(f"Failed to load models: {e}")
            raise e

    def get_uni_model(self, cluster_id):
        return self.models['uni_clusters'].get(str(cluster_id))

    def get_prog_model(self, cluster_id):
        return self.models['prog_clusters'].get(str(cluster_id))

    def get_demand_model(self, use_fallback=False):
        return self.models['demand_fallback'] if use_fallback else self.models['demand_main']

    def get_scaler(self, name):
        return self.scalers.get(name)