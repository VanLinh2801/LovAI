package com.lovai.lovaiapi.dto.push;

import com.lovai.lovaiapi.model.enums.Platform;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class PushTokenRequest {
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotNull(message = "Platform is required")
    private Platform platform;
    
    private String deviceId;
}

