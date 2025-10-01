package com.lovai.lovaiapi.exception;

public class UnauthorizedException extends RuntimeException {
    public final String code;
    public UnauthorizedException(String message) {
        super(message);
        this.code = "UNAUTHORIZED";
    }
    public UnauthorizedException(String code, String message) {
        super(message);
        this.code = code;
    }
}
