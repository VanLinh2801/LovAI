// model/NotificationEvent.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="notification_events")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationEvent {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @ManyToOne(optional=false)
    @JoinColumn(name="notification_id", nullable=false)
    private Notification notification;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(name="event_type", nullable=false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="meta", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> meta;

    @Column(name="occurred_at", insertable=false, updatable=false)
    private OffsetDateTime occurredAt;
}