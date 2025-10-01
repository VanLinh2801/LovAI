package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import com.lovai.lovaiapi.model.enums.Platform;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="push_tokens",
        uniqueConstraints = @UniqueConstraint(columnNames = "token"))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PushToken {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @ManyToOne(optional=false)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition="platform_enum", nullable=false)
    private Platform platform;

    @Column(nullable=false)
    private String token;

    @Column(name="device_id")
    private String deviceId;

    @Column(name="is_active", nullable=false)
    private boolean active;

    @Column(name="last_seen_at")
    private OffsetDateTime lastSeenAt;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;
}
