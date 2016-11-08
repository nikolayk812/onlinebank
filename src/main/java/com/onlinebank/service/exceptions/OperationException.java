package com.onlinebank.service.exceptions;

import com.onlinebank.model.operation.Operation;

/**
 * Exception thrown when operation fail due to business constraints
 */
public class OperationException extends RuntimeException {
    private final Operation operation;

    public OperationException(Operation operation, String message) {
        super(message);
        this.operation = operation;
    }

    public OperationException(Operation operation, String message, Throwable cause) {
        super(message, cause);
        this.operation = operation;
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OperationException{");
        sb.append("operation=").append(operation);
        sb.append('}');
        return sb.toString();
    }
}
