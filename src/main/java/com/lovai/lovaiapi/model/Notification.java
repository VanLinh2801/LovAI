package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.lovai.lovaiapi.model.enums.NotifChannel;
import com.lovai.lovaiapi.model.enums.NotifStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="notifications")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(name="template_code")
    private String templateCode;

    @Enumerated(EnumType.STRING)
    @Column(name="channel")
    private NotifChannel channel;

    @Column(nullable=false)
    private String title;

    @Column(nullable=false, columnDefinition="text")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="payload", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> payload;

    private String category;

    @ManyToOne
    @JoinColumn(name="created_by")
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private NotifStatus status;

    @Column(name="scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name="sent_at")
    private OffsetDateTime sentAt;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;
}
