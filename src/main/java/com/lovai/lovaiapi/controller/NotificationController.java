package com.lovai.lovaiapi.controller;

import com.lovai.lovaiapi.dto.notification.NotificationListResponse;
import com.lovai.lovaiapi.dto.notification.NotificationResponse;
import com.lovai.lovaiapi.dto.notification.CreateNotificationRequest;
import com.lovai.lovaiapi.dto.notification.MarkAsReadRequest;
import com.lovai.lovaiapi.service.NotificationService;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/create")
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.SC_CREATED).build();
    }

    @GetMapping
    public ResponseEntity<NotificationListResponse> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-User-Id") UUID userId) {
        
        NotificationListResponse response = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestHeader("X-User-Id") UUID userId) {
        long unreadCount = notificationService.countUnread(userId);
        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @PostMapping("/mark-as-read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @Valid @RequestBody MarkAsReadRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        
        notificationService.markAsRead(userId, request);
        return ResponseEntity.ok(Map.of("message", "Notifications marked as read successfully"));
    }

    @PostMapping("/mark-all-as-read")
    public ResponseEntity<Map<String, String>> markAllAsRead(@RequestHeader("X-User-Id") UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read successfully"));
    }
}
