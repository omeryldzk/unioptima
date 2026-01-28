import numpy as np # Ensure numpy is imported
from app.model_registry import ModelRegistry


def predict_ranking(uni_cluster_id: str, prog_cluster_id: str, features: list[float]) -> float:
    registry = ModelRegistry()
    
    # The scaler expects shape (n_samples, n_features)
    # We convert the list [f1, f2...] into [[f1, f2...]]
    features_2d = np.array(features).reshape(1, -1)
    
    # 1. Scale features
    scaler = registry.get_scaler('base_scaler')
    scaled_features = scaler.transform(features_2d)
    
    # 2. Run Uni + Prog cluster models
    uni_model = registry.get_uni_model(uni_cluster_id)
    prog_model = registry.get_prog_model(prog_cluster_id)
    
    # Fallback if models are missing
    if uni_model is None or prog_model is None:
        return 0.0
    
    # .predict() returns an array like [0.85], we need the float 0.85
    uni_pred = uni_model.predict(scaled_features)[0]
    prog_pred = prog_model.predict(scaled_features)[0]
    
    # 3. Average predictions
    avg_prediction = (uni_pred + prog_pred) / 2.0
    
    return float(avg_prediction)