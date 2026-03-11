package com.unioptima.backendservice.dto;

import java.util.List;

public record SimulationResponse(
        List<SimulationStep> steps,
        SimulationResult result
) {
}
