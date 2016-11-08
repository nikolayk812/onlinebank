package com.onlinebank.service.exceptions;

public class NotFoundException extends RuntimeException {
    private final String name;

    public NotFoundException(String name) {
        this.name = name;
    }

    public NotFoundException(String name, String message) {
        super(message);
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NotFoundException{");
        sb.append("name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
