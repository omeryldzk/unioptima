from app.model_registry import ModelRegistry
import numpy as np # Ensure numpy is imported


def predict_demand(use_fallback: bool, features: list[float]) -> float:
    registry = ModelRegistry()
    
    # Choose main vs fallback model via flag
    model = registry.get_demand_model(use_fallback)
    scaler_name = 'demand_fallback_scaler' if use_fallback else 'demand_main_scaler'
    scaler = registry.get_scaler(scaler_name)
    
    if model is None:
         return 0.0
    
    # Scale features if scaler is available
    features_2d = np.array(features).reshape(1, -1)

    if scaler:
        try:
            scaled_features = scaler.transform(features_2d)
        except Exception as e:
            # Fallback or log if scaling fails? For now, proceed with raw or return 0
            # Ideally we should log this. 
            print(f"Scaling failed: {e}") # Using print as no logger setup inside function, but registry has logging
            
    prediction = model.predict(scaled_features)[0]
    
    return prediction
