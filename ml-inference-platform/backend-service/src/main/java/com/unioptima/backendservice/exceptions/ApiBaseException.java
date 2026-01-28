package com.unioptima.backendservice.exceptions;

import org.springframework.http.HttpStatus;

public abstract class ApiBaseException extends RuntimeException {
    private final HttpStatus status;

    public ApiBaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}