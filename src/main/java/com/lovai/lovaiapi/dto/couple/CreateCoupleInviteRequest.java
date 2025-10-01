package com.lovai.lovaiapi.dto.couple;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CreateCoupleInviteRequest {
    
    @NotNull(message = "ID người được mời không được để trống")
    private UUID inviteeId;
    
    private OffsetDateTime expiresAt;
}
