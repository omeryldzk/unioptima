package com.unioptima.backendservice.service;

import java.util.List;

public interface BaseRankingMetadataService {
    List<String> getFeatures();
    int getFeatureCount();

}
