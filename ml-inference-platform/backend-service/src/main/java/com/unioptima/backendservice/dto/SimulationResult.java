package com.unioptima.backendservice.dto;

public record SimulationResult(String idOSYM, Double optimalQuota, Double predictedBaseRanking, Integer predictedDemand) {
}