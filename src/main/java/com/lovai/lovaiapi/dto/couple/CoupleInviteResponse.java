package com.lovai.lovaiapi.dto.couple;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.lovai.lovaiapi.dto.user.UserResponse;

@Data
public class CoupleInviteResponse {
    private UUID id;
    private UserResponse inviter;
    private UserResponse invitee;
    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime respondedAt;
}
