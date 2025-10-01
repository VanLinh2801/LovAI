package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.lovai.lovaiapi.model.enums.ActivityAction;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="activity_logs")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ActivityLog {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name="action", columnDefinition="activity_action_enum", nullable=false)
    private ActivityAction action;

    @Column(name="target_type")
    private String targetType;

    private String provider;
    @Column(name="external_id") private String externalId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="props", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> props;

    @Column(name="occurred_at", insertable=false, updatable=false)
    private OffsetDateTime occurredAt;
}
