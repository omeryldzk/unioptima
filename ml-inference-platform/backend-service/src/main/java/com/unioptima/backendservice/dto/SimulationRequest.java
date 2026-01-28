package com.unioptima.backendservice.dto;

public record SimulationRequest(
        String idOSYM,
        double minQuota,
        double maxQuota,
        double baseRankingThreshold
) {}
