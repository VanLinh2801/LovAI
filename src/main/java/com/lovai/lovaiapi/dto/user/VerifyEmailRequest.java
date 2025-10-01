package com.lovai.lovaiapi.dto.user;

import lombok.Data;

@Data
public class VerifyEmailRequest {
    private String email;
    private String otp;
}

