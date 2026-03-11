package com.unioptima.backendservice.service.impl;

import com.unioptima.backendservice.dto.SimulationStep;
import com.unioptima.backendservice.dto.SimulationTestStep;
import com.unioptima.backendservice.model.SimTestData;
import com.unioptima.backendservice.repository.SimTestDataRepository;
import com.unioptima.backendservice.service.SimulationService;
import com.unioptima.backendservice.service.SimulationTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

@Service
public class SimulationTestServiceImpl implements SimulationTestService {

    private static final Logger log = LoggerFactory.getLogger(SimulationTestServiceImpl.class);

    private final SimulationService simulationService;
    private final SimTestDataRepository simTestDataRepository;

    // Range to search for X
    private static final double MIN_VAL_X = 0.05;
    private static final double MAX_VAL_X = 0.2;

    private static final double MIN_VAL_Y = 0.05;
    private static final double MAX_VAL_Y = 0.2;
    private static final double STEP_X = 0.05;

    private static final double STEP_Y = 0.05;

    // Weight Constants
    private static final double WEIGHT_DEMAND = 0.2;
    private static final double WEIGHT_RANKING = 1.0 - WEIGHT_DEMAND;

    public SimulationTestServiceImpl(SimulationServiceImpl simulationService,
            SimTestDataRepository simTestDataRepository) {
        this.simulationService = simulationService;
        this.simTestDataRepository = simTestDataRepository;
    }

    @Override
    public List<SimulationTestStep> findBestParameter() {
        List<SimTestData> testDataList = simTestDataRepository.findAll();
        if (testDataList.isEmpty())
            return new ArrayList<>();

        // 1. Pre-calculate averages
        final double avgActualDemand = getAverage(testDataList, SimTestData::getOccupiedSlots);
        final double avgActualRanking = getAverage(testDataList, SimTestData::getBaseRanking);

        // 2. Generate list of (X, Y) pairs to test
        // We create a flattened list of all combinations to maximize parallel
        // efficiency
        List<double[]> parameterPairs = new ArrayList<>();
        for (double x = MIN_VAL_X; x <= MAX_VAL_X; x += STEP_X) {
            for (double y = MIN_VAL_Y; y <= MAX_VAL_Y; y += STEP_Y) {
                parameterPairs.add(new double[] { x, y });
            }
        }

        // 3. Execute in Parallel
        List<SimulationTestStep> results = parameterPairs.parallelStream()
                .map(pair -> calculateStepForParameters(pair[0], pair[1], testDataList, avgActualDemand,
                        avgActualRanking))
                .filter(Objects::nonNull) // Filter out failed runs
                .collect(Collectors.toList());

        // 4. Find the best result (Post-processing)
        results.stream()
                .min(Comparator.comparingDouble(SimulationTestStep::totalCost))
                .ifPresent(best -> log.info("Best Parameters found -> X: {}, Y: {} | Cost: {}",
                        String.format("%.4f", best.X()),
                        String.format("%.4f", best.Y()),
                        String.format("%.4f", best.totalCost())));

        return results;
    }

    /**
     * Helper to calculate cost for a specific (X, Y) pair.
     */
    private SimulationTestStep calculateStepForParameters(double x, double y, List<SimTestData> testDataList,
            double avgActualDemand, double avgActualRanking) {
        double totalSquaredErrorDemand = 0.0;
        double totalSquaredErrorRanking = 0.0;
        int n = 0;

        for (SimTestData testData : testDataList) {
            // Updated to pass both x and y to the simulation service
            List<SimulationStep> steps = simulationService.runQuotaOptimizationSimulationTest(
                    testData.toSimulationRequest(), x, y);

            if (steps.isEmpty())
                continue;

            SimulationStep testStep = findStepByQuota(steps, testData.getQuota());

            if (testStep != null) {
                // Demand Error
                double errorD = testStep.occupiedSlot() - testData.getOccupiedSlots();
                totalSquaredErrorDemand += errorD * errorD;

                // Ranking Error
                double errorR = testStep.predictedBaseRanking() - testData.getBaseRanking();
                totalSquaredErrorRanking += errorR * errorR;

                n++;
            }
        }

        if (n == 0)
            return null;

        // Calculate RMSE
        double rmseDemand = Math.sqrt(totalSquaredErrorDemand / n);
        double rmseRanking = Math.sqrt(totalSquaredErrorRanking / n);

        // Normalize
        double normalizedDemandError = rmseDemand / avgActualDemand;
        double normalizedRankingError = rmseRanking / avgActualRanking;

        // Total Cost
        double totalCost = (WEIGHT_DEMAND * normalizedDemandError) +
                (WEIGHT_RANKING * normalizedRankingError);

        // Return result including both X and Y
        return new SimulationTestStep(x, y, totalCost, rmseRanking, rmseDemand, normalizedRankingError,
                normalizedDemandError);
    }

    /**
     * Utility to calculate safe averages
     */
    private double getAverage(List<SimTestData> list, ToDoubleFunction<SimTestData> mapper) {
        double avg = list.stream().mapToDouble(mapper).average().orElse(1.0);
        return (avg == 0) ? 1.0 : avg;
    }

    public SimulationStep findStepByQuota(List<SimulationStep> steps, double targetQuota) {
        for (SimulationStep step : steps) {
            // Use Double.compare for safe floating point comparison
            if (Double.compare(step.quota(), targetQuota) == 0) {
                return step;
            }
        }
        return null;
    }
}