package com.lovai.lovaiapi.exception;

public class BadRequestException extends RuntimeException {
    public final String code;
    public BadRequestException(String message) {
        super(message);
        this.code = "BAD_REQUEST";
    }
    public BadRequestException(String code, String message) {
        super(message);
        this.code = code;
    }
}