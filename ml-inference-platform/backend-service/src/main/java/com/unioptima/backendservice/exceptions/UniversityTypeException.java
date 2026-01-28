package com.unioptima.backendservice.exceptions;

import org.springframework.http.HttpStatus;

public class UniversityTypeException extends ApiBaseException {
    public UniversityTypeException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
