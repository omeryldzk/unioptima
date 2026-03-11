package com.unioptima.backendservice.dto;

public record SimulationStep(
        Double quota,
        Double predictedBaseRanking,
        Double predictedDemand,
        Double occupiedSlot
) {
}
