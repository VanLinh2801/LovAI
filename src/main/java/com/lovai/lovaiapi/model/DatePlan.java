package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.lovai.lovaiapi.model.enums.PlanStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="date_plans")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DatePlan {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @ManyToOne(optional=false)
    @JoinColumn(name="couple_id", nullable=false)
    private Couple couple;

    @Column(nullable=false)
    private String title;

    @Column(name="start_time")
    private OffsetDateTime startTime;

    @Column(name="end_time")
    private OffsetDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition="plan_status_enum", nullable=false)
    private PlanStatus status;

    @Column(columnDefinition="text")
    private String note;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="weather_snapshot", columnDefinition="jsonb")
    private Map<String,Object> weatherSnapshot;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="meta", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> meta;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;

    @Column(name="deleted_at")
    private OffsetDateTime deletedAt;
}
