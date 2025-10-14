package com.lovai.lovaiapi.service;

import com.lovai.lovaiapi.dto.notification.NotificationResponse;
import com.lovai.lovaiapi.dto.notification.NotificationListResponse;
import com.lovai.lovaiapi.dto.notification.CreateNotificationRequest;
import com.lovai.lovaiapi.dto.notification.MarkAsReadRequest;
import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.NotificationRecipient;
import com.lovai.lovaiapi.model.User;
import com.lovai.lovaiapi.model.enums.NotifStatus;
import com.lovai.lovaiapi.repository.NotificationRepository;
import com.lovai.lovaiapi.repository.NotificationRecipientRepository;
import com.lovai.lovaiapi.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                              NotificationRecipientRepository notificationRecipientRepository,
                              UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationRecipientRepository = notificationRecipientRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createNotification(CreateNotificationRequest request) {
        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setBody(request.getBody());
        notification.setChannel(request.getChannel());
        notification.setTemplateCode(request.getTemplateCode());
        notification.setPayload(request.getPayload());
        notification.setCategory(request.getCategory());
        notification.setStatus(NotifStatus.PENDING);
        notification.setScheduledAt(request.getScheduledAt());
        
        if (request.getCreatedById() != null) {
            User createdBy = userRepository.findById(request.getCreatedById()).orElse(null);
            notification.setCreatedBy(createdBy);
        }
        
        notification = notificationRepository.save(notification);
        
        for (UUID recipientId : request.getRecipientIds()) {
            User recipient = userRepository.findById(recipientId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipient user not found"));
            
            NotificationRecipient recipientEntity = new NotificationRecipient();
            recipientEntity.setNotification(notification);
            recipientEntity.setUser(recipient);
            notificationRecipientRepository.save(recipientEntity);
        }
        
        return notification;
    }

    public NotificationListResponse getUserNotifications(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationRecipient> recipientPage = notificationRecipientRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        List<NotificationResponse> notifications = new ArrayList<>();
        for (NotificationRecipient recipient : recipientPage.getContent()) {
            NotificationResponse response = convertToResponse(recipient);
            notifications.add(response);
        }
        
        long unreadCount = notificationRecipientRepository.countUnreadByUserId(userId);
        
        NotificationListResponse listResponse = new NotificationListResponse();
        listResponse.setNotifications(notifications);
        listResponse.setTotalPages(recipientPage.getTotalPages());
        listResponse.setTotalElements(recipientPage.getTotalElements());
        listResponse.setCurrentPage(page);
        listResponse.setSize(size);
        listResponse.setUnreadCount(unreadCount);
        
        return listResponse;
    }

    @Transactional
    public void markAsRead(UUID userId, MarkAsReadRequest request) {
        OffsetDateTime readAt = OffsetDateTime.now();
        
        for (UUID notificationId : request.getNotificationIds()) {
            NotificationRecipient recipient = notificationRecipientRepository
                    .findByNotificationIdAndUserId(notificationId, userId);
            
            if (recipient != null && recipient.getReadAt() == null) {
                recipient.setReadAt(readAt);
                notificationRecipientRepository.save(recipient);
            }
        }
    }

    public long countUnread(UUID userId) {
        return notificationRecipientRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        OffsetDateTime readAt = OffsetDateTime.now();
        notificationRecipientRepository.markAllAsReadByUserId(userId, readAt);
    }

    private NotificationResponse convertToResponse(NotificationRecipient recipient) {
        Notification notification = recipient.getNotification();
        
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
}
