package com.lovai.lovaiapi.dto.notification;

import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.enums.NotifChannel;
import com.lovai.lovaiapi.model.enums.NotifStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
public class NotificationResponse {
    private UUID id;
    private String templateCode;
    private String title;
    private String body;
    private NotifChannel channel;
    private String category;
    private Map<String, Object> payload;
    private OffsetDateTime createdAt;
    private OffsetDateTime sentAt;
    private OffsetDateTime scheduledAt;
    private OffsetDateTime readAt;
    private NotifStatus status;
    private boolean read;
    private UUID createdById;
    private String createdByName;

    public static NotificationResponse fromEntity(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTemplateCode(notification.getTemplateCode());
        response.setTitle(notification.getTitle());
        response.setBody(notification.getBody());
        response.setChannel(notification.getChannel());
        response.setCategory(notification.getCategory());
        response.setPayload(notification.getPayload());
        response.setCreatedAt(notification.getCreatedAt());
        response.setSentAt(notification.getSentAt());
        response.setScheduledAt(notification.getScheduledAt());
        response.setStatus(notification.getStatus());
        
        if (notification.getCreatedBy() != null) {
            response.setCreatedById(notification.getCreatedBy().getId());
            response.setCreatedByName(notification.getCreatedBy().getName());
        }
        
        return response;
    }
}

