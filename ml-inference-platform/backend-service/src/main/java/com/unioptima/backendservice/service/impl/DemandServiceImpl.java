package com.unioptima.backendservice.service.impl;

import com.unioptima.backendservice.model.EncodedDemandData;
import com.unioptima.backendservice.repository.DemandMetadataRepository;
import inference.Inference.DemandRequest;

import com.unioptima.backendservice.repository.EncodedDemandDataRepository;
import com.unioptima.backendservice.service.DemandMetadataService;
import com.unioptima.backendservice.service.DemandService;
import inference.Inference;
import inference.ModelServiceGrpc;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DemandServiceImpl implements DemandService {

    private static final Logger log = LoggerFactory.getLogger(DemandServiceImpl.class);
    @GrpcClient("my-inference-service")
    private ModelServiceGrpc.ModelServiceBlockingStub modelServiceBlockingStub;

    private final DemandMetadataService metadataService;
    private final EncodedDemandDataRepository encodedDataRepository;
    public DemandServiceImpl(DemandMetadataService demandMetadataService, EncodedDemandDataRepository encodedDemandDataRepository) {
        this.metadataService = demandMetadataService;
        this.encodedDataRepository = encodedDemandDataRepository;
    }

    @Override
    public List<Double> getModelFeatures(String idOSYM) {
        var features = metadataService.getFeatures();
        log.info("Extracting features: {} for idOSYM: {}", features, idOSYM);

        var doc =  encodedDataRepository.findLatestWithSelectedFeatures(idOSYM, features);

        log.info("Found document: {}", doc);
        if (doc == null) {
            throw new RuntimeException("No EncodedDemandData found for idOSYM: " + idOSYM);
        }
        return extractFeatureVector(doc, features);
    }

    @Override
    public Double predictDemand(String idOSYM) {
       List<Double> features = getModelFeatures(idOSYM);
       boolean use_fallback = !metadataService.isMainId(idOSYM);
       return getPrediction(features, use_fallback);
    }

    public List<Double> extractFeatureVector(Document doc, List<String> featureNames) {
        List<Double> vector = new ArrayList<>();

        for (String feature : featureNames) {
            Object value = doc.get(feature);
            log.info("Processing feature: {} with value: {}", feature, value);

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

    public double getPrediction(
            List<Double> featureVector,
            boolean useFallback
    ) {
        validateInputs(featureVector);

        log.info(
                "Calling Inference Service | features={}",
                 featureVector
        );

        // Never mutate shared stubs
        var stub = modelServiceBlockingStub
                .withWaitForReady()
                .withDeadlineAfter(100, TimeUnit.SECONDS);

        Inference.DemandRequest request = Inference.DemandRequest.newBuilder()
                .addAllFeatures(featureVector)
                .setUseFallback(useFallback)
                .build();

        try {
            var response = stub.predictDemand(request);

            double prediction = response.getPrediction();
            log.info("Inference result: {}", prediction);

            return prediction;

        } catch (StatusRuntimeException e) {
            log.error(
                    "Inference Service gRPC error | status={} description={}",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription()
            );
            throw new RuntimeException("Inference service unavailable", e);

        } catch (Exception e) {
            log.error("Unexpected inference error", e);
            throw new RuntimeException("Unexpected inference failure", e);
        }
    }

    private void validateInputs(
            List<Double> featureVector
    ) {
        if (featureVector == null || featureVector.isEmpty()) {
            throw new IllegalArgumentException("Feature vector must not be null or empty");
        }

        if (featureVector.size() != metadataService.getFeatureCount()) {
            throw new IllegalArgumentException(
                    "Invalid feature count: expected "
                            +  metadataService.getFeatureCount() + " but got " + featureVector.size()
            );
        }
    }




}
