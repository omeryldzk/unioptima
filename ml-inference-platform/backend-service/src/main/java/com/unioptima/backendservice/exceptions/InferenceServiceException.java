package com.unioptima.backendservice.exceptions;

import org.springframework.http.HttpStatus;

public class InferenceServiceException extends ApiBaseException {
    public InferenceServiceException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
