package com.unioptima.backendservice.service.impl;

import com.unioptima.backendservice.model.EncodedDemandData;
import com.unioptima.backendservice.repository.DemandMetadataRepository;
import com.unioptima.backendservice.repository.EncodedDemandDataRepository;
import com.unioptima.backendservice.service.DemandMetadataService;
import com.unioptima.backendservice.service.DemandService;
import inference.ModelServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

@Service
public class DemandServiceImpl implements DemandService {
    @GrpcClient("inference-service")
    private ModelServiceGrpc.ModelServiceBlockingStub modelServiceBlockingStub;

    private final DemandMetadataService metadataService;
    private final EncodedDemandDataRepository encodedDataRepository;
    public DemandServiceImpl(DemandMetadataService demandMetadataService, EncodedDemandDataRepository encodedDemandDataRepository) {
        this.metadataService = demandMetadataService;
        this.encodedDataRepository = encodedDemandDataRepository;
    }

    public List<Double> getModelFeatures(String idOSYM) {
        var features = metadataService.getFeatures();
        var doc =  encodedDataRepository.findLatestWithSelectedFeatures(idOSYM, features);
        return extractFeatureVector(doc, features);
    }

    public List<Double> extractFeatureVector(Document doc, List<String> featureNames) {
        List<Double> vector = new ArrayList<>();

        for (String feature : featureNames) {
            Object value = doc.get(feature);

            if (value == null) {
                // DECISION: How do you handle missing data?
                // Option A: Crash (safest for data integrity)
                throw new RuntimeException("Missing required feature: " + feature);

                // Option B: Default to 0.0 (safest for uptime, risky for accuracy)
                // vector.add(0.0);
            }
            else if (value instanceof Number) {
                // SAFE CASTING: Works for Integer(10), Double(10.5), Long, Float
                vector.add(((Number) value).doubleValue());
            }
            else if (value instanceof Boolean) {
                // Convert True -> 1.0, False -> 0.0
                vector.add((Boolean) value ? 1.0 : 0.0);
            }
            else {
                // Fallback for unexpected types (Strings, etc.)
                throw new RuntimeException("Feature " + feature + " is not a number! Value: " + value);
            }
        }
        return vector;
    }




}
