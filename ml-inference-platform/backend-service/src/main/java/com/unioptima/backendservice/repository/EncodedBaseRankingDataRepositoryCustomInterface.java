package com.unioptima.backendservice.repository;

import com.unioptima.backendservice.model.EncodedBaseRankingData;
import com.unioptima.backendservice.model.EncodedDemandData;
import org.bson.Document;

import java.util.List;

public interface EncodedBaseRankingDataRepositoryCustomInterface {
    public Document findLatestWithSelectedFeatures(String idOSYM, List<String> features);
}
