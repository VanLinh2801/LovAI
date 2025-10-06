package com.lovai.lovaiapi.dto.notification;

import com.lovai.lovaiapi.model.enums.NotifChannel;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class CreateNotificationRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Body is required")
    private String body;
    
    @NotNull(message = "Channel is required")
    private NotifChannel channel;
    
    private String templateCode;
    private Map<String, Object> payload;
    private String category;
    private UUID createdById;
    private OffsetDateTime scheduledAt;
    
    @NotNull(message = "Recipients are required")
    private List<UUID> recipientIds;
}
