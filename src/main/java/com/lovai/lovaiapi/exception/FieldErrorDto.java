package com.lovai.lovaiapi.exception;

public class FieldErrorDto {
    public String field;
    public String message;

    public FieldErrorDto(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
