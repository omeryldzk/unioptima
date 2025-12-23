package com.unioptima.backendservice.service;

import java.util.List;

public interface DemandService {
    List<Double> getModelFeatures(String idOSYM);

    Double predictDemand(String idOSYM);

}
