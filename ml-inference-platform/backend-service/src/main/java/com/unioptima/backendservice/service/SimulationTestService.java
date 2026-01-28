package com.unioptima.backendservice.service;

import com.unioptima.backendservice.dto.SimulationTestStep;

import java.util.List;

public interface SimulationTestService {
    List<SimulationTestStep> findBestParameter();
}
