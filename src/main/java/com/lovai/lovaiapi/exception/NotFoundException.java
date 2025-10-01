package com.lovai.lovaiapi.exception;

public class NotFoundException extends RuntimeException {
    public final String code;

    public NotFoundException(String message) {
        super(message);
        this.code = "NOT_FOUND";
    }
    public NotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }
}
