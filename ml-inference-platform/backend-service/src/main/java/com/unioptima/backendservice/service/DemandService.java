package com.unioptima.backendservice.service;

import java.util.List;

public interface DemandService {
    List<Double> getModelFeatures(String idOSYM, Double quota);

    Double predictDemand(String idOSYM);

    Double predictDemandWithQuota(String idOSYM, Double quota);

}
