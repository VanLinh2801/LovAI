package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "plan_steps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanStep {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private DatePlan plan;

    @ManyToOne(optional = true)
    @JoinColumn(name = "place_id", nullable = true)
    private ExternalVenue place;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "action_type")
    private String actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "place_snapshot", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> placeSnapshot;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}


