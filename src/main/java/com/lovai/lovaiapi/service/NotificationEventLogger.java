package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.notification.NotificationResponse;
import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.NotificationEvent;
import com.lovai.lovaiapi.model.User;
import com.lovai.lovaiapi.repository.NotificationEventRepository;
import com.lovai.lovaiapi.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationEventLogger {

    private final NotificationEventRepository notificationEventRepository;
    private final UserRepository userRepository;

    public NotificationEventLogger(NotificationEventRepository notificationEventRepository,
                                  UserRepository userRepository) {
        this.notificationEventRepository = notificationEventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void logEnqueued(Notification notification) {
        NotificationEvent event = NotificationEvent.builder()
                .notification(notification)
                .eventType("ENQUEUED")
                .meta(createMeta("Notification queued for delivery"))
                .occurredAt(OffsetDateTime.now())
                .build();
        
        notificationEventRepository.save(event);
    }

    @Transactional
    public void logSent(Notification notification, UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("message", "Notification sent successfully");
        meta.put("delivery_time", OffsetDateTime.now());
        if (user != null) {
            meta.put("user_name", user.getName());
            meta.put("user_email", user.getEmail());
        }
        
        NotificationEvent event = NotificationEvent.builder()
                .notification(notification)
                .user(user)
                .eventType("SENT")
                .meta(meta)
                .occurredAt(OffsetDateTime.now())
                .build();
        
        notificationEventRepository.save(event);
    }

    @Transactional
    public void logFailed(Notification notification, UUID userId, String errorMessage, String errorCode) {
        User user = userRepository.findById(userId).orElse(null);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("message", "Notification delivery failed");
        meta.put("error_message", errorMessage);
        meta.put("error_code", errorCode);
        meta.put("failed_at", OffsetDateTime.now());
        if (user != null) {
            meta.put("user_name", user.getName());
            meta.put("user_email", user.getEmail());
        }
        
        NotificationEvent event = NotificationEvent.builder()
                .notification(notification)
                .user(user)
                .eventType("FAILED")
                .meta(meta)
                .occurredAt(OffsetDateTime.now())
                .build();
        
        notificationEventRepository.save(event);
    }

    @Transactional
    public void logDelivered(Notification notification, UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("message", "Notification delivered to user");
        meta.put("delivered_at", OffsetDateTime.now());
        if (user != null) {
            meta.put("user_name", user.getName());
            meta.put("user_email", user.getEmail());
        }
        
        NotificationEvent event = NotificationEvent.builder()
                .notification(notification)
                .user(user)
                .eventType("DELIVERED")
                .meta(meta)
                .occurredAt(OffsetDateTime.now())
                .build();
        
        notificationEventRepository.save(event);
    }

    @Transactional
    public void logRead(Notification notification, UUID userId) {
        User user = userRepository.findById(userId).orElse(null);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("message", "Notification read by user");
        meta.put("read_at", OffsetDateTime.now());
        if (user != null) {
            meta.put("user_name", user.getName());
            meta.put("user_email", user.getEmail());
        }
        
        NotificationEvent event = NotificationEvent.builder()
                .notification(notification)
                .user(user)
                .eventType("READ")
                .meta(meta)
                .occurredAt(OffsetDateTime.now())
                .build();
        
        notificationEventRepository.save(event);
    }

    @Transactional
    public void logPushSent(NotificationResponse notification, UUID userId, String pushToken) {
        User user = userRepository.findById(userId).orElse(null);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("message", "Push notification sent successfully");
        meta.put("push_token", pushToken);
        meta.put("sent_at", OffsetDateTime.now());
        if (user != null) {
            meta.put("user_name", user.getName());
            meta.put("user_email", user.getEmail());
        }
        
        NotificationEvent event = NotificationEvent.builder()
                .notification(convertToNotification(notification))
                .user(user)
                .eventType("PUSH_SENT")
                .meta(meta)
                .occurredAt(OffsetDateTime.now())
                .build();
        
        notificationEventRepository.save(event);
    }

    @Transactional
    public void logDeliveryFailed(NotificationResponse notification, UUID userId, String errorMessage, String errorCode) {
        User user = userRepository.findById(userId).orElse(null);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("message", "Push notification delivery failed");
        meta.put("error_message", errorMessage);
        meta.put("error_code", errorCode);
        meta.put("failed_at", OffsetDateTime.now());
        if (user != null) {
            meta.put("user_name", user.getName());
            meta.put("user_email", user.getEmail());
        }
        
        NotificationEvent event = NotificationEvent.builder()
                .notification(convertToNotification(notification))
                .user(user)
                .eventType("DELIVERY_FAILED")
                .meta(meta)
                .occurredAt(OffsetDateTime.now())
                .build();
        
        notificationEventRepository.save(event);
    }

    private Notification convertToNotification(NotificationResponse response) {
        return Notification.builder()
                .id(response.getId())
                .title(response.getTitle())
                .body(response.getBody())
                .channel(response.getChannel())
                .status(response.getStatus())
                .build();
    }

    private Map<String, Object> createMeta(String message) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("message", message);
        meta.put("timestamp", OffsetDateTime.now());
        return meta;
    }
}
