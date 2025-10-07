package com.lovai.lovaiapi.websocket;

import com.lovai.lovaiapi.dto.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class NotificationWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketController.class);

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToUser(UUID userId, NotificationResponse notification) {
        try {
            String destination = "/user/" + userId + "/queue/notifications";
            
            logger.info("Sending notification to user {} via WebSocket: {}", userId, notification.getId());
            
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.info("Successfully sent notification {} to user {} via WebSocket", 
                       notification.getId(), userId);
            
        } catch (Exception e) {
            logger.error("Failed to send notification {} to user {} via WebSocket: {}", 
                       notification.getId(), userId, e.getMessage(), e);
        }
    }

    public void sendToUsers(java.util.List<UUID> userIds, NotificationResponse notification) {
        for (UUID userId : userIds) {
            sendToUser(userId, notification);
        }
    }

    public void broadcastToAll(NotificationResponse notification) {
        try {
            String destination = "/topic/notifications";
            
            logger.info("Broadcasting notification to all users: {}", notification.getId());
            
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.info("Successfully broadcasted notification {} to all users", notification.getId());
            
        } catch (Exception e) {
            logger.error("Failed to broadcast notification {}: {}", 
                       notification.getId(), e.getMessage(), e);
        }
    }

    public void sendToCategory(String category, NotificationResponse notification) {
        try {
            String destination = "/topic/notifications/" + category;
            
            logger.info("Sending notification to category {}: {}", category, notification.getId());
            
            messagingTemplate.convertAndSend(destination, notification);
            
            logger.info("Successfully sent notification {} to category {}", 
                       notification.getId(), category);
            
        } catch (Exception e) {
            logger.error("Failed to send notification {} to category {}: {}", 
                       notification.getId(), category, e.getMessage(), e);
        }
    }
}
