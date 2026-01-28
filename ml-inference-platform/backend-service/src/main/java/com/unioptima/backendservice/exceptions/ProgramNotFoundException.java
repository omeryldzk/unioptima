package com.unioptima.backendservice.exceptions;

public class ProgramNotFoundException extends ApiBaseException {
    public ProgramNotFoundException(String message) {
        super(message, org.springframework.http.HttpStatus.NOT_FOUND);
    }
}
