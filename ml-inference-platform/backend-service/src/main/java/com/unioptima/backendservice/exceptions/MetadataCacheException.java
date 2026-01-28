package com.unioptima.backendservice.exceptions;

public class MetadataCacheException extends ApiBaseException {
    public MetadataCacheException(String message) {
        super(message, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
