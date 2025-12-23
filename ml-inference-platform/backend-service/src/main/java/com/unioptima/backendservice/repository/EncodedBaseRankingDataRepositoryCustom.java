package com.unioptima.backendservice.repository;

import org.bson.Document;

import java.util.List;

public interface EncodedBaseRankingDataRepositoryCustom {
    Document findLatestWithSelectedFeatures(String idOSYM, List<String> features);
}
