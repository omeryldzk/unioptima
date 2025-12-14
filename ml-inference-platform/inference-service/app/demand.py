from app.model_registry import ModelRegistry

def predict_demand(use_fallback: bool, features: list[float]) -> list[float]:
    registry = ModelRegistry()
    
    # Choose main vs fallback model via flag
    model = registry.get_demand_model(use_fallback)
    
    if model is None:
         return [0.0] * len(features)
    
    # Demand logic might not need scaling or has its own pipeline
    predictions = model.predict(features)
    
    return predictions
