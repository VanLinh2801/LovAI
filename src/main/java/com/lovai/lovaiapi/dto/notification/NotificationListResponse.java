package com.lovai.lovaiapi.dto.notification;

import com.lovai.lovaiapi.dto.notification.NotificationResponse;
import lombok.Data;

import java.util.List;

@Data
public class NotificationListResponse {
    private List<NotificationResponse> notifications;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int size;
    private long unreadCount;
}
