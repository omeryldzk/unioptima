package com.unioptima.backendservice.service.impl;

import com.unioptima.backendservice.model.EncodedBaseRankingData;
import com.unioptima.backendservice.repository.EncodedBaseRankingDataRepository;
import com.unioptima.backendservice.service.BaseRankingMetadataService;
import com.unioptima.backendservice.service.BaseRankingService;
import inference.ModelServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BaseRankingServiceImpl implements BaseRankingService {

    // Note: If BaseRankingService interface defines methods, we need to override
    // them.
    // Assuming it's empty like DemandService, we just add the public method here.

    @GrpcClient("inference-service")
    private ModelServiceGrpc.ModelServiceBlockingStub modelServiceBlockingStub;

    private final BaseRankingMetadataService metadataService;
    private final EncodedBaseRankingDataRepository encodedDataRepository;

    public BaseRankingServiceImpl(BaseRankingMetadataService metadataService,
            EncodedBaseRankingDataRepository encodedDataRepository) {
        this.metadataService = metadataService;
        this.encodedDataRepository = encodedDataRepository;
    }

    public List<Double> getModelFeatures(String idOSYM) {
        var features = metadataService.getFeatures();
        // Use the method available in BaseRanking repository
        // Logic: find top by id starting with, order desc
        var docOptional = encodedDataRepository.findTopByIdStartingWithOrderByIdDesc(idOSYM + "_");

        if (docOptional.isEmpty()) {
            throw new RuntimeException("No EncodedBaseRankingData found for prefix: " + idOSYM);
        }

        EncodedBaseRankingData doc = docOptional.get();
        return extractFeatureVector(doc, features);
    }

    public List<Double> extractFeatureVector(EncodedBaseRankingData doc, List<String> featureNames) {
        List<Double> vector = new ArrayList<>();
        var extraFields = doc.getExtraFields();

        for (String feature : featureNames) {
            Object value = null;

            // Check explicit fields first
            if ("academicYear".equals(feature)) {
                value = doc.getAcademicYear();
            } else if ("idOSYM".equals(feature)) {
                value = doc.getIdOSYM(); // Likely not a numerical feature but checking just in case
            } else {
                // Check map
                value = extraFields.get(feature);
            }

            if (value == null) {
                // Option A: Crash (safest for data integrity)
                throw new RuntimeException("Missing required feature: " + feature + " for doc ID: " + doc.getId());
            } else if (value instanceof Number) {
                // SAFE CASTING: Works for Integer(10), Double(10.5), Long, Float
                vector.add(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                // Convert True -> 1.0, False -> 0.0
                vector.add((Boolean) value ? 1.0 : 0.0);
            } else {
                // Fallback for unexpected types (Strings, etc.)
                throw new RuntimeException("Feature " + feature + " is not a number! Value: " + value);
            }
        }
        return vector;
    }
}
