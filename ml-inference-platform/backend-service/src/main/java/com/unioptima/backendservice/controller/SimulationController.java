package com.unioptima.backendservice.controller;


import com.unioptima.backendservice.dto.SimulationRequest;
import com.unioptima.backendservice.dto.SimulationResponse;
import com.unioptima.backendservice.dto.SimulationResult;
import com.unioptima.backendservice.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {
    private final SimulationService simulationService;

    public SimulationController(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @PostMapping("/")
    public ResponseEntity<SimulationResponse> runSimulation(
            @RequestBody SimulationRequest request
    )
    {
        SimulationResponse result = simulationService.runQuotaOptimizationSimulation(request);
        return ResponseEntity.ok(result);
    }
}
