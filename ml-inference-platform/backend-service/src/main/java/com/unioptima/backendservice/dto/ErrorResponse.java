package com.unioptima.backendservice.dto;

public record ErrorResponse (
    String code,
    String message
) {}