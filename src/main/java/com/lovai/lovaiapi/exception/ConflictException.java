package com.lovai.lovaiapi.exception;

public class ConflictException extends RuntimeException {
    public final String code;
    public ConflictException(String message) {
        super(message);
        this.code = "CONFLICT";
    }
    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }
}