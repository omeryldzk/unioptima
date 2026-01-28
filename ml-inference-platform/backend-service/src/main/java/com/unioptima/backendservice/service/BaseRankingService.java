package com.unioptima.backendservice.service;

import java.util.List;

public interface BaseRankingService {
    List<Double> getModelFeatures(String idOSYM);

    Double predictRanking(String idOSYM);



}
