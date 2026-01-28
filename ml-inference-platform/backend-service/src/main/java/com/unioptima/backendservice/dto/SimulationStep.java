package com.unioptima.backendservice.dto;

public record SimulationStep(
        String idOSYM,
        Double quota,
        Double predictedBaseRanking,
        Double predictedDemand,
        Double occupiedSlot
) {
}
