package com.unioptima.backendservice.service;

import com.unioptima.backendservice.dto.SimulationRequest;
import com.unioptima.backendservice.dto.SimulationResult;
import com.unioptima.backendservice.dto.SimulationStep;

import java.util.List;
import java.util.Optional;

public interface SimulationService {
    SimulationResult runQuotaOptimizationSimulation(SimulationRequest request);

    List<SimulationStep> runQuotaOptimizationSimulationTest(SimulationRequest request, Double testValueX, Double testValueY);
}
