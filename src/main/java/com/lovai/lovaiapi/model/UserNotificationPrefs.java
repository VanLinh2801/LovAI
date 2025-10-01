package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity @Table(name="user_notification_prefs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserNotificationPrefs {

    @Id
    @Column(name="user_id", columnDefinition="uuid")
    private UUID userId;

    @OneToOne
    @JoinColumn(name="user_id", insertable=false, updatable=false)
    private User user;

    @Column(name="in_app_enabled", nullable=false)
    private boolean inAppEnabled;

    @Column(name="push_enabled", nullable=false)
    private boolean pushEnabled;

    @Column(name="quiet_start")
    private java.time.LocalTime quietStart;

    @Column(name="quiet_end")
    private java.time.LocalTime quietEnd;

    @Column(name="dnd_enabled", nullable=false)
    private boolean dndEnabled;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="categories", columnDefinition="jsonb", nullable=false)
    private List<String> categories;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;
}
