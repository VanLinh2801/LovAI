package com.lovai.lovaiapi.dto.couple;

import com.lovai.lovaiapi.dto.user.UserResponse;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class CoupleResponse {
    private UUID id;
    private String title;
    private LocalDate anniversaryDate;
    private UserResponse user1;
    private UserResponse user2;
    private Map<String, Object> meta;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
