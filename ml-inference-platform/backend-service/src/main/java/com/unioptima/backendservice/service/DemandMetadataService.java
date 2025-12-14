package com.unioptima.backendservice.service;

import java.util.List;

public interface DemandMetadataService {
    public List<String> getFeatures();

    public boolean isMainId(String idOSYM);
}


