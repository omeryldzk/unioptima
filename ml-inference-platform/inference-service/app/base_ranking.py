from app.model_registry import ModelRegistry

def predict_ranking(uni_cluster_id: str, prog_cluster_id: str, features: list[float]) -> list[float]:
    registry = ModelRegistry()
    
    # 1. Scale features once
    scaler = registry.get_scaler('base_scaler')
    scaled_features = scaler.transform(features)
    
    # 2. Run Uni + Prog cluster models
    # Fetch models dynamically based on cluster IDs
    uni_model = registry.get_uni_model(uni_cluster_id)
    prog_model = registry.get_prog_model(prog_cluster_id)
    
    if uni_model is None or prog_model is None:
        # Fallback or error handling
        # For now, returning 0.0s if model not found for specific cluster
        return [0.0] * len(features)
    
    uni_pred = uni_model.predict(scaled_features)
    prog_pred = prog_model.predict(scaled_features)
    
    # 3. Average predictions
    # Assuming single output for simplicity, or element-wise average
    avg_predictions = [(u + p) / 2.0 for u, p in zip(uni_pred, prog_pred)]
    
    return avg_predictions
