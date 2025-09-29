// model/NotificationTemplate.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="notification_templates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"code","lang"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationTemplate {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(nullable=false) private String code;
    @Column(nullable=false) private String lang;

    @Column(name="title_tpl", nullable=false)
    private String titleTpl;

    @Column(name="body_tpl", nullable=false)
    private String bodyTpl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="payload_tpl", columnDefinition="jsonb")
    private Map<String,Object> payloadTpl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="meta", columnDefinition="jsonb", nullable=false)
    private Map<String,Object> meta;

    @Column(name="updated_at", insertable=false, updatable=false)
    private OffsetDateTime updatedAt;
}