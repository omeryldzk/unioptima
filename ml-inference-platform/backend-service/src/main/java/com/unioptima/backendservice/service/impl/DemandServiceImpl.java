package com.unioptima.backendservice.service.impl;

import com.unioptima.backendservice.exceptions.InferenceServiceException;
import com.unioptima.backendservice.exceptions.InvalidParamsException;
import com.unioptima.backendservice.exceptions.MetadataCacheException;
import com.unioptima.backendservice.exceptions.ProgramNotFoundException;
import com.unioptima.backendservice.model.EncodedDemandData;
import com.unioptima.backendservice.repository.DemandMetadataRepository;
import com.unioptima.inference.proto.Inference;
import com.unioptima.inference.proto.ModelServiceGrpc;
import com.unioptima.backendservice.model.EncodedDemandData;
import com.unioptima.inference.proto.DemandRequest;
import com.unioptima.backendservice.repository.EncodedDemandDataRepository;
import com.unioptima.backendservice.service.DemandMetadataService;
import com.unioptima.backendservice.service.DemandService;
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

    public DemandServiceImpl(DemandMetadataService demandMetadataService,
            EncodedDemandDataRepository encodedDemandDataRepository) {
        this.metadataService = demandMetadataService;
        this.encodedDataRepository = encodedDemandDataRepository;
    }

    @Override
    public List<Double> getModelFeatures(String idOSYM, Double quota) {
        var features = metadataService.getFeatures();
        if (features == null || features.isEmpty()) {
            throw new MetadataCacheException("BaseRanking metadata features are not loaded in cache.");
        }
        log.info("Extracting features: {} for idOSYM: {}", features, idOSYM);
        var doc = encodedDataRepository.findLatestWithSelectedFeatures(idOSYM, features);
        log.info("Found document: {}", doc);
        if (doc == null) {
            throw new ProgramNotFoundException("No EncodedDemandData found for idOSYM: " + idOSYM);
        }
        return extractFeatureVector(doc, features, quota);
    }

    @Override
    public Double predictDemand(String idOSYM) {
        List<Double> features = getModelFeatures(idOSYM, null);
        boolean use_fallback = !metadataService.isMainId(idOSYM);
        if (use_fallback) {
            log.info("Using fallback model for idOSYM: {}", idOSYM);
        } else {
            log.info("Using main model for idOSYM: {}", idOSYM);
        }
        return getPrediction(features, use_fallback);
    }

    @Override
    public Double predictDemandWithQuota(String idOSYM, Double quota) {
        List<Double> features = getModelFeatures(idOSYM, quota);
        boolean use_fallback = !metadataService.isMainId(idOSYM);
        if (use_fallback) {
            log.info("Using fallback model for idOSYM: {}", idOSYM);
        } else {
            log.info("Using main model for idOSYM: {}", idOSYM);
        }
        return getPrediction(features, use_fallback);
    }

    // Private helper methods

    private List<Double> extractFeatureVector(Document doc, List<String> featureNames, Double quota) {
        List<Double> vector = new ArrayList<>();

        for (String feature : featureNames) {
            Object value = doc.get(feature);
            log.info("Processing feature: {} with value: {}", feature, value);
            if (feature.equals("current_quota") && quota != null) {
                // Override with provided quota
                vector.add(quota);
            } else if (value == null) {
                // DECISION: How do you handle missing data?
                // Option A: Crash (safest for data integrity)
                throw new ProgramNotFoundException("Missing required feature: " + feature);

                // Option B: Default to 0.0 (safest for uptime, risky for accuracy)
                // vector.add(0.0);
            } else if (value instanceof Number) {
                // SAFE CASTING: Works for Integer(10), Double(10.5), Long, Float
                vector.add(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                // Convert True -> 1.0, False -> 0.0
                vector.add((Boolean) value ? 1.0 : 0.0);
            } else {
                // Fallback for unexpected types (Strings, etc.)
                throw new InvalidParamsException("Feature " + feature + " is not a number! Value: " + value);
            }
        }
        return vector;
    }

    private double getPrediction(
            List<Double> featureVector,
            boolean useFallback) {
        validateInputs(featureVector);

        log.info(
                "Calling Inference Service | features={}",
                featureVector);

        // Never mutate shared stubs
        var stub = modelServiceBlockingStub
                .withWaitForReady()
                .withDeadlineAfter(14400, TimeUnit.SECONDS);

        DemandRequest request = DemandRequest.newBuilder()
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
                    e.getStatus().getDescription());
            throw new InferenceServiceException("Inference service unavailable");

        } catch (Exception e) {
            log.error("Unexpected inference error", e);
            throw new InferenceServiceException("Unexpected inference failure");
        }
    }

    private void validateInputs(
            List<Double> featureVector) {
        if (featureVector == null || featureVector.isEmpty()) {
            throw new IllegalArgumentException("Feature vector must not be null or empty");
        }

        if (featureVector.size() != metadataService.getFeatureCount()) {
            throw new IllegalArgumentException(
                    "Invalid feature count: expected "
                            + metadataService.getFeatureCount() + " but got " + featureVector.size());
        }
    }

}
