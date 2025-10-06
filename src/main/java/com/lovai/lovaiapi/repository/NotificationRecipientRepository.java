package com.lovai.lovaiapi.repository;

import com.lovai.lovaiapi.model.NotificationRecipient;
import com.lovai.lovaiapi.model.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, UUID> {
    
    NotificationRecipient findByNotificationIdAndUserId(UUID notificationId, UUID userId);
    
    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.readAt = :readAt WHERE nr.id = :id")
    int markAsRead(@Param("id") UUID id, @Param("readAt") OffsetDateTime readAt);
    
    @Modifying
    @Query("UPDATE NotificationRecipient nr SET nr.readAt = :readAt " +
           "WHERE nr.user.id = :userId AND nr.readAt IS NULL")
    int markAllAsReadByUserId(@Param("userId") UUID userId, @Param("readAt") OffsetDateTime readAt);
    
    @Query("SELECT COUNT(nr) FROM NotificationRecipient nr " +
           "WHERE nr.user.id = :userId AND nr.readAt IS NULL")
    long countUnreadByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT nr FROM NotificationRecipient nr " +
           "WHERE nr.user.id = :userId " +
           "ORDER BY nr.notification.createdAt DESC")
    Page<NotificationRecipient> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT nr FROM NotificationRecipient nr " +
           "WHERE nr.notification.id = :notificationId " +
           "ORDER BY nr.notification.createdAt DESC")
    List<NotificationRecipient> findByNotificationIdOrderByCreatedAtDesc(@Param("notificationId") UUID notificationId);
    
    @Query("SELECT nr FROM NotificationRecipient nr " +
           "WHERE nr.notification.id = :notificationId AND nr.deliveryStatus = :status")
    List<NotificationRecipient> findByNotificationIdAndDeliveryStatus(@Param("notificationId") UUID notificationId, 
                                                                      @Param("status") DeliveryStatus status);
}
