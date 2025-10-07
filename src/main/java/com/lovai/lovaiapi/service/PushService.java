package com.lovai.lovaiapi.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.lovai.lovaiapi.dto.notification.NotificationResponse;
import com.lovai.lovaiapi.model.PushToken;
import com.lovai.lovaiapi.model.enums.Platform;
import com.lovai.lovaiapi.repository.PushTokenRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PushService {

    private static final Logger logger = LoggerFactory.getLogger(PushService.class);

    private final FirebaseMessaging firebaseMessaging;
    private final PushTokenRepository pushTokenRepository;
    private final NotificationEventLogger eventLogger;

    public PushService(FirebaseMessaging firebaseMessaging,
                      PushTokenRepository pushTokenRepository,
                      NotificationEventLogger eventLogger) {
        this.firebaseMessaging = firebaseMessaging;
        this.pushTokenRepository = pushTokenRepository;
        this.eventLogger = eventLogger;
    }

    @Transactional
    public void sendToUser(UUID userId, NotificationResponse notificationResponse) {
        try {
            List<PushToken> activeTokens = pushTokenRepository.findByUserIdAndActiveTrue(userId);
            
            if (activeTokens.isEmpty()) {
                logger.warn("No active push tokens found for user: {}", userId);
                return;
            }
            
            logger.info("Sending push notification to user {} with {} tokens", userId, activeTokens.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (PushToken pushToken : activeTokens) {
                try {
                    sendToToken(pushToken, notificationResponse);
                    successCount++;
                    
                    eventLogger.logPushSent(notificationResponse, userId, pushToken.getToken());
                    
                } catch (Exception e) {
                    failureCount++;
                    logger.error("Failed to send push notification to token {}: {}", 
                               pushToken.getToken(), e.getMessage());
                    
                    deactivateTokenOnError(pushToken, e.getMessage());
                    
                    eventLogger.logDeliveryFailed(notificationResponse, userId, e.getMessage(), "PUSH_ERROR");
                }
            }
            
            logger.info("Push notification delivery completed for user {}: {} success, {} failed", 
                       userId, successCount, failureCount);
            
        } catch (Exception e) {
            logger.error("Error sending push notification to user {}: {}", userId, e.getMessage(), e);
        }
    }

    private void sendToToken(PushToken pushToken, NotificationResponse notificationResponse) 
            throws FirebaseMessagingException {
        
        Notification notification = Notification.builder()
                .setTitle(notificationResponse.getTitle())
                .setBody(notificationResponse.getBody())
                .build();
        
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", notificationResponse.getId().toString());
        data.put("category", notificationResponse.getCategory() != null ? notificationResponse.getCategory() : "");
        data.put("channel", notificationResponse.getChannel().toString());
        data.put("createdAt", notificationResponse.getCreatedAt().toString());
        
        if (notificationResponse.getPayload() != null) {
            notificationResponse.getPayload().forEach((key, value) -> {
                if (value != null) {
                    data.put(key, value.toString());
                }
            });
        }
        
        Message message = Message.builder()
                .setToken(pushToken.getToken())
                .setNotification(notification)
                .putAllData(data)
                .build();
        
        String response = firebaseMessaging.send(message);
        logger.info("Push notification sent successfully to token {}: {}", pushToken.getToken(), response);
    }

    private void deactivateTokenOnError(PushToken pushToken, String errorMessage) {
        try {
            pushToken.setActive(false);
            pushTokenRepository.save(pushToken);
            
            logger.warn("Deactivated push token {} due to error: {}", pushToken.getToken(), errorMessage);
            
        } catch (Exception e) {
            logger.error("Error deactivating push token {}: {}", pushToken.getToken(), e.getMessage());
        }
    }

    @Transactional
    public void sendToUsers(List<UUID> userIds, NotificationResponse notificationResponse) {
        for (UUID userId : userIds) {
            sendToUser(userId, notificationResponse);
        }
    }

    @Transactional
    public void sendToUserByPlatform(UUID userId, Platform platform, NotificationResponse notificationResponse) {
        try {
            List<PushToken> platformTokens = pushTokenRepository.findByUserIdAndPlatformAndActiveTrue(userId, platform);
            
            if (platformTokens.isEmpty()) {
                logger.warn("No active {} push tokens found for user: {}", platform, userId);
                return;
            }
            
            logger.info("Sending push notification to user {} on platform {} with {} tokens", 
                       userId, platform, platformTokens.size());
            
            for (PushToken pushToken : platformTokens) {
                try {
                    sendToToken(pushToken, notificationResponse);
                    eventLogger.logPushSent(notificationResponse, userId, pushToken.getToken());
                    
                } catch (Exception e) {
                    logger.error("Failed to send push notification to {} token {}: {}", 
                               platform, pushToken.getToken(), e.getMessage());
                    deactivateTokenOnError(pushToken, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error sending push notification to user {} on platform {}: {}", 
                       userId, platform, e.getMessage(), e);
        }
    }

    public void sendTestNotification(String token, String title, String body) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();
            
            Map<String, String> data = new HashMap<>();
            data.put("type", "test");
            data.put("timestamp", OffsetDateTime.now().toString());
            
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .putAllData(data)
                    .build();
            
            String response = firebaseMessaging.send(message);
            logger.info("Test push notification sent successfully: {}", response);
            
        } catch (Exception e) {
            logger.error("Error sending test push notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send test push notification", e);
        }
    }
}
