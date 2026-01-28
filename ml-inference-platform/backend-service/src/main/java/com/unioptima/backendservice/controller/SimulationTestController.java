package com.unioptima.backendservice.controller;


import com.unioptima.backendservice.dto.SimulationRequest;
import com.unioptima.backendservice.dto.SimulationResult;
import com.unioptima.backendservice.dto.SimulationTestStep;
import com.unioptima.backendservice.service.SimulationTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/simulation-test")
public class SimulationTestController {

    private final SimulationTestService simulationTestService;
    public SimulationTestController(SimulationTestService simulationTestService) {
        this.simulationTestService = simulationTestService;
    }

    @PostMapping("/")
    public ResponseEntity<List<SimulationTestStep>> runSimulation(
    )
    {
        List<SimulationTestStep> steps = simulationTestService.findBestParameter();
        return ResponseEntity.ok(steps);
    }
}
