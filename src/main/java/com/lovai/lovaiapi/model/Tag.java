package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="tags")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Tag {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(nullable=false, unique = true)
    private String code;

    @Column(nullable=false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="meta", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> meta;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;
}
