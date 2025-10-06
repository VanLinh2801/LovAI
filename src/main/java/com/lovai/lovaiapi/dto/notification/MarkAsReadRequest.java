package com.lovai.lovaiapi.dto.notification;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Data
public class MarkAsReadRequest {
    @NotNull(message = "Notification IDs are required")
    private List<UUID> notificationIds;
}
