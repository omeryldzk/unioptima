package com.unioptima.backendservice.service.impl;

import com.unioptima.backendservice.model.EncodedBaseRankingData;
import inference.Inference.BaseRankingRequest;
import com.unioptima.backendservice.repository.EncodedBaseRankingDataRepository;
import com.unioptima.backendservice.service.BaseRankingMetadataService;
import com.unioptima.backendservice.service.BaseRankingService;
import inference.ModelServiceGrpc;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BaseRankingServiceImpl implements BaseRankingService {

    // Note: If BaseRankingService interface defines methods, we need to override
    // them.
    // Assuming it's empty like DemandService, we just add the public method here.

    private static final Logger log = LoggerFactory.getLogger(BaseRankingServiceImpl.class);


    @GrpcClient("my-inference-service")
    private ModelServiceGrpc.ModelServiceBlockingStub modelServiceBlockingStub;

    private final BaseRankingMetadataService metadataService;
    private final EncodedBaseRankingDataRepository encodedDataRepository;

    public BaseRankingServiceImpl(BaseRankingMetadataService metadataService,
            EncodedBaseRankingDataRepository encodedDataRepository) {
        this.metadataService = metadataService;
        this.encodedDataRepository = encodedDataRepository;
    }

    @Override
    public Double predictRanking(String idOSYM){
        String uniId = encodedDataRepository.findTopByIdStartingWithOrderByIdDesc(idOSYM + "_")
                .map(data -> data.getExtraFields().get("university_cluster").toString())
                .orElseThrow(() -> new RuntimeException("university_cluster not found for idOSYM: " + idOSYM));
        String programId = encodedDataRepository.findTopByIdStartingWithOrderByIdDesc(idOSYM + "_")
                .map(data -> data.getExtraFields().get("program_cluster").toString())
                .orElseThrow(() -> new RuntimeException("program_cluster not found for idOSYM: " + idOSYM));
        List<Double> features = getModelFeatures(idOSYM);
        // Call inference service
        return getPrediction(features, uniId, programId);
    }

    @Override
    public List<Double> getModelFeatures(String idOSYM) {
        var features = metadataService.getFeatures();
        log.info("Extracting features: {} for idOSYM: {}", features, idOSYM);
        // Use the method available in BaseRanking repository
        // Logic: find top by id starting with, order desc
        var docOptional = encodedDataRepository.findTopByIdStartingWithOrderByIdDesc(idOSYM + "_");
        log.info("Found document: {}", docOptional);
        if (docOptional.isEmpty()) {
            throw new RuntimeException("No EncodedBaseRankingData found for prefix: " + idOSYM);
        }
        EncodedBaseRankingData doc = docOptional.get();
        return extractFeatureVector(doc, features);
    }



    public List<Double> extractFeatureVector(EncodedBaseRankingData doc, List<String> featureNames) {
        List<Double> vector = new ArrayList<>();
        var extraFields = doc.getExtraFields();
        log.info("Extracting features from doc ID: {} with extraFields: {}", doc.getId(), extraFields);

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
                log.info("Feature: {} | Value from map: {}", feature, value);
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

    public double getPrediction(
            List<Double> featureVector,
            String uniId,
            String programId
    ) {
        validateInputs(featureVector, uniId, programId);

        log.info(
                "Calling Inference Service | uniId={}, programId={}, features={}",
                uniId, programId, featureVector
        );

        // Never mutate shared stubs
        var stub = modelServiceBlockingStub
                .withWaitForReady()
                .withDeadlineAfter(100, TimeUnit.SECONDS);

        BaseRankingRequest request = BaseRankingRequest.newBuilder()
                .setUniClusterId(uniId)
                .setProgClusterId(programId)
                .addAllFeatures(featureVector)
                .build();

        try {
            var response = stub.predictBaseRanking(request);

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
            List<Double> featureVector,
            String uniId,
            String programId
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

        if (uniId == null || uniId.isBlank()) {
            throw new IllegalArgumentException("uniId must not be blank");
        }

        if (programId == null || programId.isBlank()) {
            throw new IllegalArgumentException("programId must not be blank");
        }
    }
}
