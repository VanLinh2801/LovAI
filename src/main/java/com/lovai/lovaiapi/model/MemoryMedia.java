// model/MemoryMedia.java
package com.lovai.lovaiapi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity @Table(name="memory_media")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MemoryMedia {

    @Id @UuidGenerator
    @Column(columnDefinition="uuid")
    private UUID id;

    @ManyToOne(optional=false)
    @JoinColumn(name="memory_id", nullable=false)
    private Memory memory;

    @Column(nullable=false)
    private String url;

    @Column(name="media_type") // DB có CHECK ('image','video','audio','file')
    private String mediaType;

    private Integer width;
    private Integer height;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="exif_json", columnDefinition="jsonb")
    private Map<String,Object> exifJson;

    @Column(name="created_at", insertable=false, updatable=false)
    private OffsetDateTime createdAt;
}
