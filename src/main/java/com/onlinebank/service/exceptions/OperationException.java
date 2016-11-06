package com.onlinebank.service.exceptions;

//TODO: add to REST mapping
public class OperationException extends RuntimeException {
    public OperationException(String message) {
        super(message);
    }

    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
