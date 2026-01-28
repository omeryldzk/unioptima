package com.unioptima.backendservice.exceptions;

public class InvalidParamsException extends ApiBaseException {
    public InvalidParamsException(String message) {
        super(message, org.springframework.http.HttpStatus.BAD_REQUEST);
    }

}
