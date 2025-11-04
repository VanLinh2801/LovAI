package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.Couple;
import com.lovai.lovaiapi.model.enums.NotifStatus;
import com.lovai.lovaiapi.model.enums.NotifChannel;
import com.lovai.lovaiapi.dto.notification.CreateNotificationRequest;
import com.lovai.lovaiapi.repository.NotificationRepository;
import com.lovai.lovaiapi.repository.CoupleRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Service
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationRepository notificationRepository;
    private final CoupleRepository coupleRepository;
    private final DeliveryService deliveryService;
    private final NotificationEventLogger eventLogger;
    private final NotificationService notificationService;

    public NotificationScheduler(NotificationRepository notificationRepository,
                                CoupleRepository coupleRepository,
                                DeliveryService deliveryService,
                                NotificationEventLogger eventLogger,
                                NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.coupleRepository = coupleRepository;
        this.deliveryService = deliveryService;
        this.eventLogger = eventLogger;
        this.notificationService = notificationService;
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
            try {
                if (notification.getStatus() == NotifStatus.PENDING) {
                    notification.setStatus(NotifStatus.SENT);
                    notification.setSentAt(OffsetDateTime.now());
                    notificationRepository.save(notification);
                }
            } catch (Exception e) {
                logger.warn("Failed to update notification {} to SENT after delivery: {}", notification.getId(), e.getMessage());
            }
            
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

    @Scheduled(cron = "0 30 7 * * *")
    @Transactional
    public void processAnniversaryNotifications() {
        try {
            logger.info("Starting anniversary notification processing...");
            
            LocalDate today = LocalDate.now();
            List<Couple> couples = coupleRepository.findAll();
            
            int notificationsCreated = 0;
            
            for (Couple couple : couples) {
                if (couple.getDeletedAt() != null || couple.getAnniversaryDate() == null) {
                    continue;
                }
                
                try {
                    notificationsCreated += createAnniversaryNotifications(couple, today);
                } catch (Exception e) {
                    logger.error("Error creating anniversary notifications for couple {}: {}", 
                               couple.getId(), e.getMessage(), e);
                }
            }
            
            logger.info("Created {} anniversary notifications", notificationsCreated);
            
        } catch (Exception e) {
            logger.error("Critical error in anniversary notification processing: {}", e.getMessage(), e);
        }
    }

    private int createAnniversaryNotifications(Couple couple, LocalDate today) {
        int notificationsCreated = 0;
        
        if (shouldCreateReminderNotification(couple, today, 3)) {
            createReminderNotification(couple, 3);
            notificationsCreated++;
        }
        
        if (shouldCreateAnniversaryNotification(couple, today)) {
            createAnniversaryNotification(couple);
            notificationsCreated++;
        }
        
        notificationsCreated += createMilestoneNotifications(couple, today);
        
        return notificationsCreated;
    }

    private boolean shouldCreateReminderNotification(Couple couple, LocalDate today, int daysBefore) {
        LocalDate targetDate = couple.getAnniversaryDate().withYear(today.getYear());
        if (targetDate.isBefore(today)) {
            targetDate = targetDate.plusYears(1);
        }
        
        LocalDate reminderDate = targetDate.minusDays(daysBefore);
        return today.equals(reminderDate);
    }

    private boolean shouldCreateAnniversaryNotification(Couple couple, LocalDate today) {
        LocalDate anniversaryThisYear = couple.getAnniversaryDate().withYear(today.getYear());
        return today.equals(anniversaryThisYear);
    }

    private void createReminderNotification(Couple couple, int daysBefore) {
        try {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTitle("💕 Nhắc nhở ngày kỷ niệm sắp tới!");
            request.setBody("🎉 Còn " + daysBefore + " ngày nữa là đến ngày kỷ niệm của bạn! Hãy chuẩn bị một điều bất ngờ nhé! ✨");
            request.setChannel(NotifChannel.IN_APP);
            request.setTemplateCode("ANNIVERSARY_REMINDER");
            request.setCategory("ANNIVERSARY");
            request.setScheduledAt(OffsetDateTime.of(LocalDate.now(), LocalTime.of(7, 30), ZoneOffset.UTC));
            
            List<UUID> recipientIds = new ArrayList<>();
            recipientIds.add(couple.getUser1().getId());
            if (couple.getUser2() != null) {
                recipientIds.add(couple.getUser2().getId());
            }
            request.setRecipientIds(recipientIds);
            
            Notification notification = notificationService.createNotification(request);
            deliveryService.sendInAppNotification(notification);
            
            logger.info("Created reminder notification for couple {} ({} days before)", 
                       couple.getId(), daysBefore);
            
        } catch (Exception e) {
            logger.error("Failed to create reminder notification for couple {}: {}", 
                       couple.getId(), e.getMessage(), e);
        }
    }

    private void createAnniversaryNotification(Couple couple) {
        try {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTitle("🎉 Chúc mừng ngày kỷ niệm!");
            request.setBody("💕 Hôm nay là ngày kỷ niệm của bạn! Chúc hai bạn mãi mãi hạnh phúc! ✨");
            request.setChannel(NotifChannel.IN_APP);
            request.setTemplateCode("ANNIVERSARY_CELEBRATION");
            request.setCategory("ANNIVERSARY");
            request.setScheduledAt(OffsetDateTime.of(LocalDate.now(), LocalTime.of(7, 30), ZoneOffset.UTC));
            
            List<UUID> recipientIds = new ArrayList<>();
            recipientIds.add(couple.getUser1().getId());
            if (couple.getUser2() != null) {
                recipientIds.add(couple.getUser2().getId());
            }
            request.setRecipientIds(recipientIds);
            
            Notification notification = notificationService.createNotification(request);
            deliveryService.sendInAppNotification(notification);
            
            logger.info("Created anniversary notification for couple {}", couple.getId());
            
        } catch (Exception e) {
            logger.error("Failed to create anniversary notification for couple {}: {}", 
                       couple.getId(), e.getMessage(), e);
        }
    }

    private int createMilestoneNotifications(Couple couple, LocalDate today) {
        int notificationsCreated = 0;
        LocalDate anniversaryDate = couple.getAnniversaryDate();
        
        int yearsSinceAnniversary = today.getYear() - anniversaryDate.getYear();
        if (anniversaryDate.isAfter(today.minusYears(yearsSinceAnniversary))) {
            yearsSinceAnniversary--;
        }
        
        if (shouldCreateMilestoneNotification(couple, today, 1, 0)) { 
            createMilestoneNotification(couple, "1 tuần", "🎉 Chúc mừng 1 tuần yêu nhau!");
            notificationsCreated++;
        }
        
        if (shouldCreateMilestoneNotification(couple, today, 1, 1)) { 
            createMilestoneNotification(couple, "1 tháng", "💕 Chúc mừng 1 tháng yêu nhau!");
            notificationsCreated++;
        }
        
        if (shouldCreateMilestoneNotification(couple, today, 3, 1)) { 
            createMilestoneNotification(couple, "3 tháng", "🎊 Chúc mừng 3 tháng yêu nhau!");
            notificationsCreated++;
        }
        
        if (shouldCreateMilestoneNotification(couple, today, 6, 1)) { 
            createMilestoneNotification(couple, "6 tháng", "💖 Chúc mừng 6 tháng yêu nhau!");
            notificationsCreated++;
        }
        
        if (shouldCreateMilestoneNotification(couple, today, 1, 12)) {
            createMilestoneNotification(couple, "1 năm", "🏆 Chúc mừng 1 năm yêu nhau!");
            notificationsCreated++;
        }
        
        if (yearsSinceAnniversary > 0 && shouldCreateYearlyNotification(couple, today)) {
            createYearlyNotification(couple, yearsSinceAnniversary);
            notificationsCreated++;
        }
        
        return notificationsCreated;
    }

    private boolean shouldCreateMilestoneNotification(Couple couple, LocalDate today, int months, int weeks) {
        LocalDate targetDate = couple.getAnniversaryDate().plusMonths(months).plusWeeks(weeks);
        return today.equals(targetDate);
    }

    private boolean shouldCreateYearlyNotification(Couple couple, LocalDate today) {
        LocalDate anniversaryThisYear = couple.getAnniversaryDate().withYear(today.getYear());
        return today.equals(anniversaryThisYear);
    }

    private void createMilestoneNotification(Couple couple, String milestone, String title) {
        try {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTitle(title);
            request.setBody("✨ " + milestone + " đã trôi qua! Hãy tiếp tục yêu thương nhau nhé! 💕");
            request.setChannel(NotifChannel.IN_APP);
            request.setTemplateCode("MILESTONE_" + milestone.replace(" ", "_").toUpperCase());
            request.setCategory("MILESTONE");
            request.setScheduledAt(OffsetDateTime.of(LocalDate.now(), LocalTime.of(7, 30), ZoneOffset.UTC));
            
            List<UUID> recipientIds = new ArrayList<>();
            recipientIds.add(couple.getUser1().getId());
            if (couple.getUser2() != null) {
                recipientIds.add(couple.getUser2().getId());
            }
            request.setRecipientIds(recipientIds);
            
            Notification notification = notificationService.createNotification(request);
            deliveryService.sendInAppNotification(notification);
            
            logger.info("Created milestone notification for couple {}: {}", couple.getId(), milestone);
            
        } catch (Exception e) {
            logger.error("Failed to create milestone notification for couple {}: {}", 
                       couple.getId(), e.getMessage(), e);
        }
    }

    private void createYearlyNotification(Couple couple, int years) {
        try {
            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTitle("🎊 Chúc mừng " + years + " năm yêu nhau!");
            request.setBody("💕 " + years + " năm đã trôi qua! Chúc hai bạn mãi mãi hạnh phúc! ✨");
            request.setChannel(NotifChannel.IN_APP);
            request.setTemplateCode("YEARLY_ANNIVERSARY");
            request.setCategory("ANNIVERSARY");
            request.setScheduledAt(OffsetDateTime.of(LocalDate.now(), LocalTime.of(7, 30), ZoneOffset.UTC));
            
            List<UUID> recipientIds = new ArrayList<>();
            recipientIds.add(couple.getUser1().getId());
            if (couple.getUser2() != null) {
                recipientIds.add(couple.getUser2().getId());
            }
            request.setRecipientIds(recipientIds);
            
            Notification notification = notificationService.createNotification(request);
            deliveryService.sendInAppNotification(notification);
            
            logger.info("Created yearly notification for couple {}: {} years", couple.getId(), years);
            
        } catch (Exception e) {
            logger.error("Failed to create yearly notification for couple {}: {}", 
                       couple.getId(), e.getMessage(), e);
        }
    }
}
