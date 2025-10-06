package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {
    
    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.notification.id = :notificationId ORDER BY ne.occurredAt DESC")
    List<NotificationEvent> findByNotificationId(@Param("notificationId") UUID notificationId);
    
    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.user.id = :userId ORDER BY ne.occurredAt DESC")
    List<NotificationEvent> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.eventType = :eventType ORDER BY ne.occurredAt DESC")
    List<NotificationEvent> findByEventType(@Param("eventType") String eventType);
    
    @Query("SELECT ne FROM NotificationEvent ne WHERE ne.notification.id = :notificationId AND ne.user.id = :userId ORDER BY ne.occurredAt DESC")
    List<NotificationEvent> findByNotificationIdAndUserId(@Param("notificationId") UUID notificationId, @Param("userId") UUID userId);
}
