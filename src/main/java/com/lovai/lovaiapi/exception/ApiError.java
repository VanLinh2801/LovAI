package com.lovai.lovaiapi.exception;

import java.time.OffsetDateTime;
import java.util.List;

public class ApiError {
    public int status;
    public String error;
    public String message;
    public String path;
    public String code;
    public OffsetDateTime timestamp;
    public List<FieldErrorDto> details;

    public ApiError(int status, String error, String message, String path, String code, List<FieldErrorDto> details) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.code = code;
        this.details = details;
        this.timestamp = OffsetDateTime.now();
    }
}
