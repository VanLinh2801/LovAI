package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.Notification;
import com.lovai.lovaiapi.model.NotificationRecipient;
import com.lovai.lovaiapi.model.enums.NotifStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    @Query("SELECT n FROM Notification n " +
           "JOIN NotificationRecipient nr ON n.id = nr.notification.id " +
           "WHERE nr.user.id = :userId " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT COUNT(nr) FROM NotificationRecipient nr " +
           "WHERE nr.user.id = :userId " +
           "AND nr.readAt IS NULL")
    long countUnreadByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT nr FROM NotificationRecipient nr " +
           "WHERE nr.user.id = :userId " +
           "ORDER BY nr.notification.createdAt DESC")
    Page<NotificationRecipient> findRecipientsByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT nr FROM NotificationRecipient nr " +
           "WHERE nr.user.id = :userId " +
           "AND nr.readAt IS NULL " +
           "ORDER BY nr.notification.createdAt DESC")
    List<NotificationRecipient> findUnreadRecipientsByUserId(@Param("userId") UUID userId);
    
    List<Notification> findByStatusAndScheduledAtLessThanEqual(NotifStatus status, OffsetDateTime scheduledAt);
    
    long countByStatusAndScheduledAtLessThanEqual(NotifStatus status, OffsetDateTime scheduledAt);

    // Find scheduled notifications for a specific plan by reading planId from JSONB payload
    @Query(value = "SELECT * FROM notifications n WHERE n.template_code = :templateCode AND n.status = :status AND n.payload->>'planId' = :planId", nativeQuery = true)
    List<Notification> findScheduledByTemplateAndPlanId(@Param("templateCode") String templateCode,
                                                        @Param("status") String status,
                                                        @Param("planId") String planId);
}
