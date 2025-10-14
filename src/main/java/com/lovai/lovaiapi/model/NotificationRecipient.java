package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.lovai.lovaiapi.model.enums.DeliveryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="notification_recipients")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationRecipient {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @ManyToOne(optional=false)
    @JoinColumn(name="notification_id", nullable=false)
    private Notification notification;

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name="delivery_status")
    private DeliveryStatus deliveryStatus;

    @Column(name="delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name="read_at")
    private OffsetDateTime readAt;

    @Column(name="error_code")
    private String errorCode;

    @Column(name="retry_count", nullable=false)
    private int retryCount;
}
