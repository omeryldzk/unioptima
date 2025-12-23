package com.unioptima.backendservice.service;

import java.util.List;

public interface DemandMetadataService {
    List<String> getFeatures();

    boolean isMainId(String idOSYM);

    int getFeatureCount();
}


