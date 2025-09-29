// model/Memory.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="memories")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Memory {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @ManyToOne(optional=false)
    @JoinColumn(name="couple_id", nullable=false)
    private Couple couple;

    @Column(nullable=false)
    private String title;

    @Column(columnDefinition="text")
    private String description;

    @Column(name="happened_at")
    private OffsetDateTime happenedAt;

    @Column(name="location_text")
    private String locationText;

    @Column(name="media_count", nullable=false)
    private int mediaCount;

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
