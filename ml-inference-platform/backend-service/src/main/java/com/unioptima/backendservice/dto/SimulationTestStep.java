package com.unioptima.backendservice.dto;

public record SimulationTestStep (
        double X,

        double Y,
        double totalCost,

        double rankingError,

        double demandError,
        double normalizedRankingError,
        double normalizedDemandError
) {
}
