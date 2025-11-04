package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.notification.NotificationResponse;
import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.NotificationRecipient;
import com.lovai.lovaiapi.model.enums.DeliveryStatus;
import com.lovai.lovaiapi.model.enums.NotifStatus;
import com.lovai.lovaiapi.repository.NotificationRecipientRepository;
import com.lovai.lovaiapi.repository.NotificationRepository;
import com.lovai.lovaiapi.websocket.NotificationWebSocketController;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final NotificationEventLogger eventLogger;
    private final NotificationWebSocketController webSocketController;
    private final PushService pushService;
    private final SimpUserRegistry simpUserRegistry;

    public DeliveryService(NotificationRepository notificationRepository,
                          NotificationRecipientRepository notificationRecipientRepository,
                          NotificationEventLogger eventLogger,
                          NotificationWebSocketController webSocketController,
                          PushService pushService,
                          SimpUserRegistry simpUserRegistry) {
        this.notificationRepository = notificationRepository;
        this.notificationRecipientRepository = notificationRecipientRepository;
        this.eventLogger = eventLogger;
        this.webSocketController = webSocketController;
        this.pushService = pushService;
        this.simpUserRegistry = simpUserRegistry;
    }

    @Async("notificationTaskExecutor")
    @Transactional
    public void sendInAppNotification(Notification notification) {
        try {
            logger.info("Starting delivery for notification: {}", notification.getId());
            
            List<NotificationRecipient> recipients = notificationRecipientRepository
                    .findByNotificationIdOrderByCreatedAtDesc(notification.getId());
            
            if (recipients.isEmpty()) {
                logger.warn("No recipients found for notification: {}", notification.getId());
                return;
            }
            
            boolean allDelivered = true;
            for (NotificationRecipient recipient : recipients) {
                try {
                    sendToRecipient(notification, recipient);
                } catch (Exception e) {
                    logger.error("Failed to send notification {} to user {}: {}", 
                               notification.getId(), recipient.getUser().getId(), e.getMessage());
                    allDelivered = false;
                    
                    eventLogger.logFailed(notification, recipient.getUser().getId(), 
                                        e.getMessage(), "DELIVERY_ERROR");
                }
            }
            
            if (allDelivered) {
                notification.setStatus(NotifStatus.SENT);
                notification.setSentAt(OffsetDateTime.now());
                notificationRepository.save(notification);
                
                logger.info("Successfully delivered notification: {}", notification.getId());
            } else {
                notification.setStatus(NotifStatus.SENT);
                notification.setSentAt(OffsetDateTime.now());
                notificationRepository.save(notification);
                
                logger.warn("Partially delivered notification: {} (some recipients failed)", 
                           notification.getId());
            }
            
        } catch (Exception e) {
            logger.error("Critical error delivering notification {}: {}", 
                       notification.getId(), e.getMessage(), e);
            
            notification.setStatus(NotifStatus.CANCELED);
            notificationRepository.save(notification);
        }
    }

    private void sendToRecipient(Notification notification, NotificationRecipient recipient) {
        try {
            UUID userId = recipient.getUser().getId();
            logger.info("Delivering notification {} to user {}", notification.getId(), userId);
            
            NotificationResponse notificationResponse = convertToNotificationResponse(notification, recipient);
            
            boolean isUserOnline = isUserOnline(userId);
            boolean websocketSent = false;
            boolean pushSent = false;
            
            // Always send push notification via Firebase
            try {
                logger.info("Sending push notification to user {} via Firebase", userId);
                pushService.sendToUser(userId, notificationResponse);
                pushSent = true;
                logger.info("Successfully sent push notification {} to user {} via Firebase", 
                           notification.getId(), userId);
            } catch (Exception e) {
                logger.error("Failed to send push notification {} to user {}: {}", 
                           notification.getId(), userId, e.getMessage());
            }
            
            // Also send via WebSocket if user is online
            if (isUserOnline) {
                try {
                    logger.info("User {} is online, also sending via WebSocket", userId);
                    webSocketController.sendToUser(userId, notificationResponse);
                    websocketSent = true;
                    logger.info("Successfully sent notification {} to user {} via WebSocket", 
                               notification.getId(), userId);
                } catch (Exception e) {
                    logger.error("Failed to send WebSocket notification {} to user {}: {}", 
                               notification.getId(), userId, e.getMessage());
                }
            }
            
            // Mark as delivered if at least one method succeeded
            if (pushSent || websocketSent) {
                recipient.setDeliveryStatus(DeliveryStatus.DELIVERED);
                recipient.setDeliveredAt(OffsetDateTime.now());
                notificationRecipientRepository.save(recipient);
                
                eventLogger.logSent(notification, userId);
                eventLogger.logDelivered(notification, userId);
                
                logger.info("Notification {} delivered to user {} - Push: {}, WebSocket: {}", 
                           notification.getId(), userId, pushSent, websocketSent);
            } else {
                // If both failed, throw exception to trigger retry
                throw new RuntimeException("Both push and WebSocket delivery failed");
            }
            
        } catch (Exception e) {
            recipient.setDeliveryStatus(DeliveryStatus.FAILED);
            recipient.setErrorCode("DELIVERY_ERROR");
            recipient.setRetryCount(recipient.getRetryCount() + 1);
            notificationRecipientRepository.save(recipient);
            
            throw e;
        }
    }

    @Async("notificationTaskExecutor")
    @Transactional
    public void retryFailedDeliveries(Notification notification) {
        List<NotificationRecipient> failedRecipients = notificationRecipientRepository
                .findByNotificationIdAndDeliveryStatus(notification.getId(), DeliveryStatus.FAILED);
        
        for (NotificationRecipient recipient : failedRecipients) {
            if (recipient.getRetryCount() < 3) {
                try {
                    sendToRecipient(notification, recipient);
                    logger.info("Retry successful for notification {} to user {}", 
                               notification.getId(), recipient.getUser().getId());
                } catch (Exception e) {
                    logger.error("Retry failed for notification {} to user {}: {}", 
                               notification.getId(), recipient.getUser().getId(), e.getMessage());
                }
            } else {
                logger.warn("Max retries exceeded for notification {} to user {}", 
                           notification.getId(), recipient.getUser().getId());
            }
        }
    }

    private NotificationResponse convertToNotificationResponse(Notification notification, NotificationRecipient recipient) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTemplateCode(notification.getTemplateCode());
        response.setChannel(notification.getChannel());
        response.setTitle(notification.getTitle());
        response.setBody(notification.getBody());
        response.setPayload(notification.getPayload());
        response.setCategory(notification.getCategory());
        response.setStatus(notification.getStatus());
        response.setScheduledAt(notification.getScheduledAt());
        response.setSentAt(notification.getSentAt());
        response.setCreatedAt(notification.getCreatedAt());
        response.setReadAt(recipient.getReadAt());
        response.setRead(recipient.getReadAt() != null);
        
        if (notification.getCreatedBy() != null) {
            response.setCreatedById(notification.getCreatedBy().getId());
            response.setCreatedByName(notification.getCreatedBy().getName());
        }
        
        return response;
    }

    private boolean isUserOnline(UUID userId) {
        try {
            return simpUserRegistry.getUsers().stream()
                    .anyMatch(user -> {
                        String sessionUserId = user.getName();
                        try {
                            return sessionUserId != null && UUID.fromString(sessionUserId).equals(userId);
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    });
        } catch (Exception e) {
            logger.warn("Error checking if user {} is online: {}", userId, e.getMessage());
            return false;
        }
    }
}
