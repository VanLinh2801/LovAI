package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.enums.NotifStatus;
import com.lovai.lovaiapi.repository.NotificationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationRepository notificationRepository;
    private final DeliveryService deliveryService;
    private final NotificationEventLogger eventLogger;

    public NotificationScheduler(NotificationRepository notificationRepository,
                                DeliveryService deliveryService,
                                NotificationEventLogger eventLogger) {
        this.notificationRepository = notificationRepository;
        this.deliveryService = deliveryService;
        this.eventLogger = eventLogger;
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void processPendingNotifications() {
        try {
            logger.debug("Starting scheduled notification processing...");
            
            OffsetDateTime now = OffsetDateTime.now();
            
            List<Notification> pendingNotifications = notificationRepository
                    .findByStatusAndScheduledAtLessThanEqual(NotifStatus.PENDING, now);
            
            if (pendingNotifications.isEmpty()) {
                logger.debug("No pending notifications to process");
                return;
            }
            
            logger.info("Found {} pending notifications to process", pendingNotifications.size());
            
            for (Notification notification : pendingNotifications) {
                try {
                    processNotification(notification);
                } catch (Exception e) {
                    logger.error("Error processing notification {}: {}", 
                               notification.getId(), e.getMessage(), e);
                }
            }
            
            logger.info("Completed processing {} notifications", pendingNotifications.size());
            
        } catch (Exception e) {
            logger.error("Critical error in notification scheduler: {}", e.getMessage(), e);
        }
    }

    private void processNotification(Notification notification) {
        try {
            logger.info("Processing notification: {} (scheduled at: {})", 
                       notification.getId(), notification.getScheduledAt());
            
            eventLogger.logEnqueued(notification);
            
            deliveryService.sendInAppNotification(notification);
            
            logger.info("Successfully queued notification {} for delivery", notification.getId());
            
        } catch (Exception e) {
            logger.error("Failed to process notification {}: {}", 
                       notification.getId(), e.getMessage(), e);
            
            handleFailedNotification(notification, e);
        }
    }

    private void handleFailedNotification(Notification notification, Exception error) {
        try {
            logger.warn("Notification {} failed to process, will retry in next cycle", 
                       notification.getId());
            
            
        } catch (Exception e) {
            logger.error("Error handling failed notification {}: {}", 
                       notification.getId(), e.getMessage(), e);
        }
    }

    @Transactional
    public void processNotificationNow(UUID notificationId) {
        try {
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
            
            if (notification.getStatus() != NotifStatus.PENDING) {
                logger.warn("Notification {} is not in PENDING status, current status: {}", 
                           notificationId, notification.getStatus());
                return;
            }
            
            processNotification(notification);
            
        } catch (Exception e) {
            logger.error("Error manually processing notification {}: {}", 
                       notificationId, e.getMessage(), e);
            throw e;
        }
    }

    public long getPendingNotificationCount() {
        OffsetDateTime now = OffsetDateTime.now();
        return notificationRepository.countByStatusAndScheduledAtLessThanEqual(NotifStatus.PENDING, now);
    }
}
