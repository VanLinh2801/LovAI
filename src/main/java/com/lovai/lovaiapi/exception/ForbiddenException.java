package com.lovai.lovaiapi.exception;

public class ForbiddenException extends RuntimeException {
    public final String code;
    public ForbiddenException(String message) {
        super(message);
        this.code = "FORBIDDEN";
    }
    public ForbiddenException(String code, String message) {
        super(message);
        this.code = code;
    }
}