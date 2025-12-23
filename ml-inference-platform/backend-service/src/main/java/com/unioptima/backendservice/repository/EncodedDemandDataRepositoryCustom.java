package com.unioptima.backendservice.repository;

import org.bson.Document;

import java.util.List;

public interface EncodedDemandDataRepositoryCustom {
    Document findLatestWithSelectedFeatures(String idOSYM, List<String> features);
}
