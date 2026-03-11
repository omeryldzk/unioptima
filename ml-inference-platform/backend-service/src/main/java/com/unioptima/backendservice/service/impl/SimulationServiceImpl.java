package com.unioptima.backendservice.service.impl;

import com.unioptima.backendservice.dto.SimulationRequest;
import com.unioptima.backendservice.dto.SimulationResponse;
import com.unioptima.backendservice.dto.SimulationResult;
import com.unioptima.backendservice.dto.SimulationStep;
import com.unioptima.backendservice.exceptions.InvalidParamsException;
import com.unioptima.backendservice.exceptions.ProgramNotFoundException;
import com.unioptima.backendservice.exceptions.UniversityTypeException;
import com.unioptima.backendservice.repository.RawDataRepository;
import com.unioptima.backendservice.service.BaseRankingService;
import com.unioptima.backendservice.service.DemandService;
import com.unioptima.backendservice.service.SimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.abs;

@Service
public class SimulationServiceImpl implements SimulationService {
    private static final Logger log = LoggerFactory.getLogger(SimulationServiceImpl.class);

    private final DemandService demandService;
    private final BaseRankingService baseRankingService;

    private final RawDataRepository rawDataRepository;

    public SimulationServiceImpl(DemandService demandService, BaseRankingService baseRankingService,
            RawDataRepository RawDataRepository) {
        this.demandService = demandService;
        this.baseRankingService = baseRankingService;
        this.rawDataRepository = RawDataRepository;
    }

    @Override
    public SimulationResponse runQuotaOptimizationSimulation(SimulationRequest request) {
        String idOSYM = request.idOSYM();
        double minQuota = request.minQuota();
        double maxQuota = request.maxQuota();
        double baseRankingThreshold = request.baseRankingThreshold();

        if(!validateParams(minQuota, maxQuota, baseRankingThreshold)) {
            throw new InvalidParamsException("Invalid simulation parameters provided.");
        }
        // 1. Fetch RawData
        var rawDataOptional = rawDataRepository.findTopByIdStartingWithOrderByIdDesc(idOSYM + "_");
        if (rawDataOptional.isEmpty()) {
            throw new ProgramNotFoundException("No Historical Data found for idOSYM: " + idOSYM);
        }
        var rawData = rawDataOptional.get();

        // 2. Extract Historical/Lag Data
        Double lagBaseRankingAvg = getDoubleFromExtra(rawData, "lag_baseRanking_avg");
        Double lagTopRankingAvg = getDoubleFromExtra(rawData, "lag_topRanking_avg");
        Double lagOccupiedSlotAvg = getDoubleFromExtra(rawData, "lag_occupiedslot_avg");
        Double P = getDoubleFromExtra(rawData, "P");
        Double U = getDoubleFromExtra(rawData, "U");
        Double year = rawData.getAcademicYear().doubleValue();
        Double deltaRankAvg = getDoubleFromExtra(rawData, "delta_rank_avg");
        String universityType = (String) rawData.getExtraFields().get("universityType");

        if (P == null || U == null) {
            throw new ProgramNotFoundException(
                    "Insufficient historical data (P or U missing) for simulation for idOSYM: " + idOSYM);
        }
        // 1. Calculate the Quality Factor
        double qualityFactor = Math.max(P * U, 0.1); // Clamp to 0.05 minimum

        // 2. Select Parameters Dynamically
        // X: Controls how fast we reach saturation.
        // We want X to be smaller when quality is low to widen the linear range of tanh.
        double powerParameterX = 0.01;

        double powerParameterY = 0.5;

        if(universityType.equals("devlet")){
            throw new UniversityTypeException("Simulation not supported for 'devlet' university type for idOSYM: " + idOSYM);
        }

        log.info("Using {} year data for idOSYM: {}", year, idOSYM);
        if (lagBaseRankingAvg == null || lagTopRankingAvg == null || lagOccupiedSlotAvg == null || P == null
                || U == null || deltaRankAvg == null) {
            throw new ProgramNotFoundException(
                    "Insufficient historical data for simulation for idOSYM: " + idOSYM);
        }


        // 3. Predict Base Ranking (Once)
        Double predictedBaseRankingStart = baseRankingService.predictRanking(idOSYM);

        // 4. Simulation Loop
        // "Start the simulation from minimum sim_quota and continue until
        // max_sim_quota"
        // Return "one step before result" if threshold exceeded.

        SimulationResult bestResult = null;
        List<SimulationStep> simulationSteps = new java.util.ArrayList<>();
        SimulationStep simulationStep;
        double powerTerm ;
        log.info("Running simulation for idOSYM: {} with RawData: {}", idOSYM, rawData);
        for (double currentQuota = minQuota; currentQuota <= maxQuota; currentQuota += 1.0) {
            // Predict demand
            Double predictedDemand = demandService.predictDemandWithQuota(idOSYM, currentQuota);

            // current_occupiedSlot = min(sim_quota, predicted_demand)
            double currentOccupiedSlot = Math.min(currentQuota, predictedDemand);

            // Δslot = current_occupiedSlot - lag_occupiedSlot
            double deltaSlot = currentOccupiedSlot - lagOccupiedSlotAvg;
            log.info("Delta Slot {} ", deltaSlot);

            // Δslot_sat = tanh(Δslot / lag_occupiedslot_avg)
            double deltaSlotSat = Math.tanh(((deltaSlot) * powerParameterX) / qualityFactor) ;
            log.info("Delta Slot Sat {} ", deltaSlotSat);

            if(deltaSlotSat < 0.0){
                deltaSlotSat = abs(deltaSlotSat);
                powerTerm = Math.exp(powerParameterY * deltaSlotSat / qualityFactor) * -1;
            }
            else {
                powerTerm = Math.exp(powerParameterY * deltaSlotSat / qualityFactor);
            }

            log.info("Power Term {} ", powerTerm);
            log.info("P and U values: {} , {} ", P, U);

            double baseRankingChange =  deltaRankAvg * powerTerm;
            log.info("Base Ranking Change {} ", baseRankingChange);
            // sim_baseRanking = predicted_baseRanking + baseRanking_change
            double simBaseRanking = predictedBaseRankingStart + baseRankingChange;
            simBaseRanking = Math.min(simBaseRanking, 2000000.0); // Clamp to 2,000,000 worst rank
            log.info("Simulated Base Ranking {} for Quota {} ", simBaseRanking, currentQuota);

            // Check threshold
            // If threshold exceeded stop the loop abort to one step before result.
            if (simBaseRanking > baseRankingThreshold) {
                // Return previous valid result if exists.
                // If it fails on the very first step, bestResult might be null.
                // The prompt says "abort to one step before".
                if (bestResult != null) {
                    return new SimulationResponse(simulationSteps, bestResult);
                } else {
                    // First step already exceeded threshold.
                    // Return the current one? Or Empty?
                    // "abort to one step before result" implies the previous one was valid.
                    // If no previous one, maybe this config is impossible within threshold.
                    throw new InvalidParamsException(
                            "Simulation exceeded threshold on first step; no valid configuration found. Please change parameters.");
                }
            }
            // 5. Check  demand vs occupied slots for best result
            int simulatedOccupiedSlots = (int) Math.round(currentOccupiedSlot);
            simulationStep = new SimulationStep(currentQuota, simBaseRanking, predictedDemand, currentOccupiedSlot);
            simulationSteps.add(simulationStep);
            if (bestResult != null) {
                int bestOccupiedSlots = bestResult.predictedDemand();
                if (simulatedOccupiedSlots <= bestOccupiedSlots) {
                    continue; // Skip updating bestResult if current is worse
                }
            }
            // Update best result (this step is valid)
            bestResult = new SimulationResult(idOSYM, currentQuota, simBaseRanking, (int) Math.round(currentOccupiedSlot));
            log.info("Current Best Result: {}", bestResult);
        }

        // If we finish the loop without exceeding, return the last result and simulation steps
        return new SimulationResponse(simulationSteps,bestResult);
    }

    private Double getDoubleFromExtra(com.unioptima.backendservice.model.RawData data, String key) {
        Object val = data.getExtraFields().get(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        return null;
    }

    private boolean validateParams(double minQuota, double maxQuota, double baseRankingThreshold) {
        if (minQuota < 0 || maxQuota < 0 || baseRankingThreshold < 0) {
            return false;
        }
        return minQuota <= maxQuota;
    }

    @Override
    public List<SimulationStep> runQuotaOptimizationSimulationTest(SimulationRequest request, Double powerParameterX, Double powerParameterY) {
        String idOSYM = request.idOSYM();
        double minQuota = request.minQuota();
        double maxQuota = request.maxQuota();
        double baseRankingThreshold = request.baseRankingThreshold();
        if(!validateParams(minQuota, maxQuota, baseRankingThreshold)) {
            throw new InvalidParamsException("Invalid simulation parameters provided.");
        }
        // 1. Fetch RawData
        var rawDataOptional = rawDataRepository.findTopByIdStartingWithOrderByIdDesc(idOSYM + "_2023");
        if (rawDataOptional.isEmpty()) {
            throw new ProgramNotFoundException("No Historical Data found for idOSYM: " + idOSYM);
        }
        var rawData = rawDataOptional.get();

        // 2. Extract Historical/Lag Data
        Double lagBaseRankingAvg = getDoubleFromExtra(rawData, "lag_baseRanking_avg");
        Double lagTopRankingAvg = getDoubleFromExtra(rawData, "lag_topRanking_avg");
        Double lagOccupiedSlotAvg = getDoubleFromExtra(rawData, "lag_occupiedslot_avg");
        Double P = getDoubleFromExtra(rawData, "P");
        Double U = getDoubleFromExtra(rawData, "U");
        Double year = rawData.getAcademicYear().doubleValue();
        Double deltaRankAvg = getDoubleFromExtra(rawData, "delta_rank_avg");
        String universityType = (String) rawData.getExtraFields().get("universityType");

        // 1. Calculate the Quality Factor
        double qualityFactor = Math.max(P * U, 0.1); // Clamp to 0.1 minimum

        if(universityType.equals("devlet")){
            throw new UniversityTypeException("Simulation not supported for 'devlet' university type for idOSYM: " + idOSYM);
        }

        log.info("Using {} year data for idOSYM: {}", year, idOSYM);
        if (lagBaseRankingAvg == null || lagTopRankingAvg == null || lagOccupiedSlotAvg == null || P == null
                || U == null || deltaRankAvg == null) {
            throw new ProgramNotFoundException(
                    "Insufficient historical data for simulation for idOSYM: " + idOSYM);
        }

        // 3. Predict Base Ranking (Once)
        Double predictedBaseRankingStart = baseRankingService.predictRanking(idOSYM);

        // 4. Simulation Loop
        // "Start the simulation from minimum sim_quota and continue until
        // max_sim_quota"
        // Return "one step before result" if threshold exceeded.
        List<SimulationStep> results = new java.util.ArrayList<>();
        SimulationStep resultStep = null;
        log.info("Running simulation for idOSYM: {} with RawData: {}", idOSYM, rawData);
        for (double currentQuota = minQuota; currentQuota <= maxQuota; currentQuota += 1.0) {
            // Predict demand
            Double predictedDemand = demandService.predictDemandWithQuota(idOSYM, currentQuota);

            // current_occupiedSlot = min(sim_quota, predicted_demand)
            double currentOccupiedSlot = Math.min(currentQuota, predictedDemand);

            // Δslot = current_occupiedSlot - lag_occupiedSlot
            double deltaSlot = currentOccupiedSlot - lagOccupiedSlotAvg;
            log.info("Delta Slot {} ", deltaSlot);

            // Δslot_sat = tanh(Δslot / lag_occupiedslot_avg)
            double deltaSlotSat = Math.tanh((deltaSlot * powerParameterX) / qualityFactor) ;
            log.info("Delta Slot Sat {} ", deltaSlotSat);

            double powerTerm = Math.exp(powerParameterY * deltaSlotSat / qualityFactor);
            log.info("Power Term {} ", powerTerm);
            log.info("P and U values: {} , {} ", P, U);

            double baseRankingChange =  deltaRankAvg * powerTerm ;
            log.info("Base Ranking Change {} ", baseRankingChange);
            // sim_baseRanking = predicted_baseRanking + baseRanking_change
            double simBaseRanking = predictedBaseRankingStart + baseRankingChange;
            log.info("Simulated Base Ranking {} for Quota {} ", simBaseRanking, currentQuota);

            // Check threshold
            // If threshold exceeded stop the loop abort to one step before result.
            if (simBaseRanking > baseRankingThreshold) {
                // Return previous valid result if exists.
                // If it fails on the very first step, bestResult might be null.
                // The prompt says "abort to one step before".
                if (resultStep != null) {
                    return results;
                } else {
                    // First step already exceeded threshold.
                    // Return the current one? Or Empty?
                    // "abort to one step before result" implies the previous one was valid.
                    // If no previous one, maybe this config is impossible within threshold.
                    throw new InvalidParamsException(
                            "Simulation exceeded threshold on first step; no valid configuration found. Please change parameters.");
                }
            }

            // Update best result (this step is valid)
            resultStep = new SimulationStep(currentQuota, simBaseRanking, predictedDemand, currentOccupiedSlot);
            log.info("Current Best Result: {}", resultStep);
            results.add(resultStep);
        }

        // If we finish the loop without exceeding, return the last result
        return results;
    }
}
